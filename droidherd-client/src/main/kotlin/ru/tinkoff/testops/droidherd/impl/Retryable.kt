package ru.tinkoff.testops.droidherd.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

object Retryable {
    suspend fun <T> with(
        times: Int = 5,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        maxTimeMs: Long = 0,
        exceptionCallback: (t: Exception, attempt: Int) -> Unit = { _, _ -> },
        block: suspend () -> T
    ): T {

        var currentDelay = initialDelay
        val startTime = System.currentTimeMillis()
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                exceptionCallback(e, it + 1)
                validateMaxTimeout(startTime, maxTimeMs, it + 1, e)
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return try {
            block() // last attempt
        } catch (e: Exception) {
            exceptionCallback(e, times)
            throw e // even if exception callback not throw - will rethrow exception
        }
    }

    private fun validateMaxTimeout(startTime: Long, maxTime: Long, attempt: Int, lastException: Exception) {
        if (maxTime > 0 && (System.currentTimeMillis() > (startTime + maxTime))) {
            throw CancellationException("Max time $maxTime (ms) exceeded after attempt: $attempt", lastException)
        }
    }
}
