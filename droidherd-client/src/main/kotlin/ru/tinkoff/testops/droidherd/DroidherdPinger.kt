package ru.tinkoff.testops.droidherd

import org.slf4j.LoggerFactory
import ru.tinkoff.testops.droidherd.impl.DroidherdApiProvider
import java.util.*
import java.util.concurrent.TimeUnit

class DroidherdPinger(val api: DroidherdApiProvider) {
    companion object {
        private val PERIOD = TimeUnit.MINUTES.toMillis(4)
        private val logger = LoggerFactory.getLogger(DroidherdPinger::class.java)
    }

    private val timer = Timer()

    private val isDebug = "true" == System.getenv("FORK_FARM_SESSION_DEBUG")

    fun start() {
        if (!isDebug) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    logger.debug("Pinging the orchestrator")
                    api.ping()
                }
            }, PERIOD, PERIOD)
        }
    }

    fun finish() {
        if (!isDebug) {
            timer.cancel()
        }
    }
}
