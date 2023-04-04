package ru.tinkoff.testops.droidherd.impl

import kotlinx.coroutines.runBlocking
import retrofit2.Response
import ru.tinkoff.testops.droidherd.DroidherdClientApi
import ru.tinkoff.testops.droidherd.RetrofitProvider
import ru.tinkoff.testops.droidherd.api.DroidherdClientMetric
import ru.tinkoff.testops.droidherd.api.DroidherdSessionStatus
import ru.tinkoff.testops.droidherd.api.EmulatorRequest
import ru.tinkoff.testops.droidherd.api.SessionRequest
import ru.tinkoff.testops.droidherd.auth.AuthProvider

class DroidherdApiProviderImpl(
    retrofitProvider: RetrofitProvider,
    private val authProvider: AuthProvider,
) : DroidherdApiProvider {
    private val retrofit = retrofitProvider.provide().create(DroidherdClientApi::class.java)

    private lateinit var sessionId: String

    private fun authHeader() = authProvider.get().token
    private fun clientId() = authProvider.get().clientId

    private class NoRetryHttpException(message: String) : Exception(message)

    private val httpExceptionCallback = { exception: Exception, _: Int ->
        if (exception is NoRetryHttpException) {
            throw exception
        }
    }

    private suspend fun <T> retry(block: suspend () -> T) = Retryable.with(
        times = 5, initialDelay = 5000,
        exceptionCallback = httpExceptionCallback,
        block = block)

    private fun <T> handleResponse(response: Response<T>) {
        if (response.code() in 400..499) {
            throw NoRetryHttpException(
                "${response.message()}, code: ${response.code()}, error body: " +
                        "${response.errorBody()?.string()}"
            )
        }
        if (!response.isSuccessful) {
            val errorBody = response.raw().body()?.toString()
            throw RuntimeException("Response not successful: ${response.code()}, $errorBody")
        }
    }

    override fun login() = runBlocking {
        val loginFunc = {
            retrofit.login(authHeader(), clientId())
        }
        sessionId =
            retry {
                loginFunc().execute().let {
                    handleResponse(it)
                    it.body()?.sessionId
                        ?: throw IllegalStateException("No session id found in response: ${it.body().toString()}")
                }
            }
        // use stdout due to gradle plugin can run with any logging level
        println("Droidherd session id acquired: $sessionId")
    }

    override fun release() {
        runBlocking {
            retry {
                handleResponse(retrofit.release(authHeader(), clientId(), sessionId).execute())
            }
        }
    }

    override fun status(): DroidherdSessionStatus = runBlocking {
        retry {
            retrofit.status(authHeader(), clientId(), sessionId).execute().let {
                handleResponse(it)
                it.body() ?: throw IllegalStateException("No status found in response: ${it.body().toString()}")
            }
        }
    }

    override fun request(request: SessionRequest): List<EmulatorRequest> = runBlocking {
        retry {
            retrofit.scale(
                authHeader(),
                clientId(),
                sessionId,
                request
            )
                .execute().let {
                    handleResponse(it)
                    it.body() ?: throw IllegalStateException("No requests status in response: ${it.body().toString()}")
                }
        }
    }

    override fun ping() {
        runBlocking {
            retry {
                handleResponse(retrofit.ping(authHeader(), clientId(), sessionId).execute())
            }
        }
    }

    override fun postMetrics(metrics: List<DroidherdClientMetric>) {
        if (metrics.isNotEmpty()) {
            runBlocking {
                retry {
                    handleResponse(
                        retrofit.postMetrics(authHeader(), clientId(), sessionId, metrics).execute()
                    )
                }
            }
        }
    }

    override fun apiVersion() = DroidherdApiProvider.Version.v1
}
