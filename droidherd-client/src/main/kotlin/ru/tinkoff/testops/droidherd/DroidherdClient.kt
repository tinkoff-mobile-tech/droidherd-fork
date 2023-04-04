package ru.tinkoff.testops.droidherd

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tinkoff.testops.droidherd.api.*
import ru.tinkoff.testops.droidherd.impl.DroidherdApiProvider
import java.io.BufferedReader
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.system.measureTimeMillis

class DroidherdClient(
    private val adbPath: String,
    private val droidherdApi: DroidherdApiProvider,
    val config: DroidherdConfig
) {
    private val connectedEmulatorsIds = mutableSetOf<String>()
    private val logger: Logger = LoggerFactory.getLogger(DroidherdClient::class.java)
    private var normalizedMinimumRequiredEmulator = config.minimumRequiredEmulators
    private var normalizedRequiredEmulators = config.emulators
    private var emulatorsPendingTime: Long = -1
    private val emulatorParameters = config.emulatorParameters.map {
        EmulatorParameter().apply {
            name = it.key
            value = it.value
        }
    }.toList()
    private val isDebug: Boolean = System.getenv("DROIDHERD_DEBUG") == "true"
    private val releaseSessionShutdownHook = object : Thread() {
        override fun run() {
            logger.info("Abnormal exit, release session")
            releaseEmulators()
        }
    }
    private val implementationVersion: String = javaClass.`package`.implementationVersion ?: "UNKNOWN"
    private var pinger: DroidherdPinger = DroidherdPinger(droidherdApi)

    @Volatile
    private var loginSuccess = false

    fun run(testCasesCount: Int) {
        val emulators = prepareEmulators(testCasesCount)
        connectToEmulators(emulators)
        checkMinimumEmulatorsRequired()
    }

    fun connectedEmulatorsCount() = connectedEmulatorsIds.size

    private fun checkMinimumEmulatorsRequired() {
        if (connectedEmulatorsIds.size < normalizedMinimumRequiredEmulator) {
            releaseEmulators()
            throw Exception("not enough emulators to run build")
        }
    }

    fun releaseEmulators() {
        if ("true" == System.getenv("FORK_NO_RELEASE_EMULATORS")) {
            logger.error("FORK_NO_RELEASE_EMULATORS = true, skip release")
            return
        }
        if (loginSuccess) {
            logger.info("Release session")
            runCatching { pinger.finish() }
            runCatching { Runtime.getRuntime().removeShutdownHook(releaseSessionShutdownHook) }
            runCatching {
                droidherdApi.release()
            }.onFailure {
                logger.warn("Failed to release session", it)
            }
            val builder = ProcessBuilder(adbPath, "disconnect")
            val result = builder.start().waitFor()
            logger.info("adb disconnect result code: $result")
        }
    }

    fun postMetrics(metrics: DroidherdClientMetricCollector) {
        metrics.add(
            DroidherdClientMetricCollector.Key.PendingEmulatorsDurationMs,
            emulatorsPendingTime.toDouble())
        droidherdApi.postMetrics(metrics.all)
    }

    private fun prepareEmulators(testCasesCount: Int): Collection<Emulator> {
        emulatorsPendingTime = -1
        connectedEmulatorsIds.clear()

        try {
            logger.info("Logging to droidherd ...")
            droidherdApi.login()
        } catch (e: Exception) {
            logger.error("Login to farm failed", e)
            throw RuntimeException("Login to farm failed: ${e.message}", e)
        }

        loginSuccess = true
        Runtime.getRuntime().addShutdownHook(releaseSessionShutdownHook)
        pinger = DroidherdPinger(droidherdApi).apply {
            start()
        }

        requestEmulators(testCasesCount)

        val emulators: Collection<Emulator>
        emulatorsPendingTime = measureTimeMillis {
            emulators = waitRequestedEmulators()
        }
        return emulators
    }

    private fun waitRequestedEmulators(): Collection<Emulator> {
        logger.info("Wait for emulators to scale")
        Thread.sleep(TimeUnit.MINUTES.toMillis(1))

        val fourMinutes = TimeUnit.MINUTES.toMillis(4)
        var total = fourMinutes
        val frequency = TimeUnit.SECONDS.toMillis(15)
        var emulators: Collection<Emulator> = listOf()
        while (total != TimeUnit.SECONDS.toMillis(0)) {
            if (total != fourMinutes) {
                Thread.sleep(frequency)
            }
            emulators = droidherdApi.status().emulators
            if (emulators.size == totalRequiredEmulators()) {
                break
            } else {
                total -= frequency
            }
        }
        return emulators
    }

    private fun requestEmulators(testCasesCount: Int) {
        normalizeEmulators(testCasesCount)
        val requests = normalizedRequiredEmulators.map { emulator ->
            EmulatorRequest(emulator.key, emulator.value)
        }
        logger.info("Requesting emulators: {}", requests)
        val result = droidherdApi.request(
            SessionRequest(
                generateClientAttributes(),
                requests,
                emulatorParameters,
                isDebug
            )
        )
        logger.info("Request emulators result: $result")

        val providedEmulators = result.sumOf { it.quantity }
        if (providedEmulators < normalizedMinimumRequiredEmulator) {
            throw IllegalStateException("No emulators available at all. Requested minimum: $normalizedMinimumRequiredEmulator, available: $providedEmulators")
        }
    }

    private fun normalizeEmulators(testCasesCount: Int) {
        if (config.emulators.size == 1 && config.emulators.values.first() > testCasesCount) {
            normalizedRequiredEmulators = config.emulators.mapValues { testCasesCount }
            normalizedMinimumRequiredEmulator = min(config.minimumRequiredEmulators, testCasesCount)
        } else {
            normalizedRequiredEmulators = config.emulators
            normalizedMinimumRequiredEmulator = config.minimumRequiredEmulators
        }
    }

    private fun totalRequiredEmulators(): Int = normalizedRequiredEmulators.map { it.value }.sum()

    private fun connectToEmulators(emulators: Collection<Emulator>) {
        println("Tries to connect emulators: $emulators")
        emulators.forEach { emulator ->
            connectEmulator(emulator.id, emulator.adb)
        }
        logger.info("Connection finished")
    }

    private fun connectEmulator(id: String, ip: String) {
        val maxAttempts = 5
        val waitPeriod = Duration.ofSeconds(5)
        var attempts = 0
        do {
            logger.info("${attempts.plus(1)} attempt to connect emulator $ip")
            if (executeAdbConnectIsSuccessful(ip)) {
                val adbDevicesResult = executeAdbDevices()
                adbDevicesResult.forEach { line -> logger.info(line as String) }
                if (adbDevicesResult.contains("$ip\tdevice")) {
                    logger.info("Emulator $ip connected")
                    connectedEmulatorsIds.add(id)
                    break
                } else {
                    Thread.sleep(waitPeriod.toMillis())
                    attempts += 1
                }
            }
        } while (attempts <= maxAttempts)
    }

    private fun executeAdbConnectIsSuccessful(ip: String): Boolean {
        val builder = ProcessBuilder(adbPath, "connect", ip)
        logger.info("adb connect $ip")
        val process = builder.start()
        val returnCode = process.waitFor()
        if (returnCode != 0) {
            logger.error("adb connect command finished with $returnCode")
            return false
        }
        return true
    }

    private fun executeAdbDevices(): Array<Any> {
        val builder = ProcessBuilder(adbPath, "devices")
        builder.redirectErrorStream(true)
        val process = builder.start()
        val inputStream = process.inputStream
        val reader = BufferedReader(inputStream.reader())
        return reader.lines().toArray()
    }


    private fun generateCiAttributes(): CiAttributes? {
        val envMap = System.getenv()
        val isCiRun = "true".equals(envMap.getOrDefault("GITLAB_CI", "false"), true) ||
                "true".equals(envMap.getOrDefault("GITLAB_CI_LIKE_META", "false"), true)
        return if (isCiRun) {
            CiAttributes(
                "gitlab",
                envMap.getOrDefault("CI_COMMIT_REF_NAME", "unknown"),
                envMap.getOrDefault("CI_PROJECT_NAME", "unknown"),
                envMap.getOrDefault("CI_JOB_URL", ""),
                envMap.getOrDefault("GITLAB_USER_LOGIN", "unknown")
            )
        } else {
            null
        }
    }

    private fun generateClientAttributes(): ClientAttributes {
        return ClientAttributes().apply {
            info = "fork-${config.clientType}"
            version = implementationVersion
            ci = generateCiAttributes()
            metadata = mapOf()
        }
    }
}

