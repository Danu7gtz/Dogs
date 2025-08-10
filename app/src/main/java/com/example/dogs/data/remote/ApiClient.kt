package com.example.dogs.data.remote

import android.content.Context
import com.example.dogs.data.TokenProvider
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ApiClient {

    private val gson by lazy {
        val iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        GsonBuilder()
            .registerTypeAdapter(
                LocalDateTime::class.java,
                JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.format(iso)) }
            )
            .registerTypeAdapter(
                LocalDateTime::class.java,
                JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString, iso) }
            )
            .create()
    }

    fun auth(baseUrl: String = "https://crm.solbintec.com/"): AuthService {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val ok = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ok)
            .build()
            .create(AuthService::class.java)
    }

    fun appointments(context: Context, baseUrl: String = "https://crm.solbintec.com/"): AppointmentService {
        val tokenProvider = TokenProvider(context)

        val authInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder().apply {
                tokenProvider.getToken()?.let { header("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(req)
        }

        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val ok = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(ok)
            .build()
            .create(AppointmentService::class.java)
    }
}