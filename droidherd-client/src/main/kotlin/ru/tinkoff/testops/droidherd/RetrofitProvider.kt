package ru.tinkoff.testops.droidherd

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitProvider(
        private val url: String
) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(RetrofitProvider::class.java)
    }

    fun provide(): Retrofit {
        val gson = createGson()
        val okHttpClient = createOkHttpClient()
        return Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if ("true" == System.getenv("FORK_ENABLE_HTTP_LOGGER")) {
                val logger = HttpLoggingInterceptor.Logger { message -> LOG.info(message) }
                val interceptor = HttpLoggingInterceptor(logger)
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)
            }
        }.build()
    }

    private fun createGson(): Gson {
        return GsonBuilder().setLenient().create()
    }
}
