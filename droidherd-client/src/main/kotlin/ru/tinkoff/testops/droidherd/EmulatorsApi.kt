package ru.tinkoff.testops.droidherd

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ru.tinkoff.testops.droidherd.api.DroidherdClientMetric
import ru.tinkoff.testops.droidherd.api.DroidherdSessionStatus
import ru.tinkoff.testops.droidherd.api.EmulatorRequest
import ru.tinkoff.testops.droidherd.api.SessionRequest

internal interface DroidherdClientApi {
    data class LoginResponse(val sessionId: String)

    @POST("api/v1/clients/{clientId}/login")
    fun login(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String
    ): Call<LoginResponse>


    @POST("api/v1/clients/{clientId}/sessions/{sessionId}")
    fun scale(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String,
        @Path("sessionId") sessionId: String,
        @Body requests: SessionRequest
    ): Call<List<EmulatorRequest>>

    @DELETE("api/v1/clients/{clientId}/sessions/{sessionId}")
    fun release(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String,
        @Path("sessionId") sessionId: String
    ): Call<ResponseBody>

    @GET("api/v1/clients/{clientId}/sessions/{sessionId}")
    fun status(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String,
        @Path("sessionId") sessionId: String
    ): Call<DroidherdSessionStatus>

    @POST("api/v1/clients/{clientId}/sessions/{sessionId}/ping")
    fun ping(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String,
        @Path("sessionId") sessionId: String
    ): Call<Void>

    @POST("api/v1/clients/{clientId}/sessions/{sessionId}/metrics")
    fun postMetrics(
        @Header("Authorization") token: String,
        @Path("clientId") clientId: String,
        @Path("sessionId") sessionId: String,
        @Body metrics: List<@JvmSuppressWildcards DroidherdClientMetric>
    ): Call<Void>
}
