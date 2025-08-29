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
import java.util.concurrent.TimeUnit

object ApiClient {

    // --- Gson consistente (ISO_LOCAL_DATE_TIME, sin zona) ---
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

    // --- Cliente para Auth (sin header Authorization) ---
    fun auth(baseUrl: String = "https://crm.solbintec.com/"): AuthService {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            // Evita imprimir Authorization por si en el futuro lo agregas
            redactHeader("Authorization")
        }
        val ok = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ok)
            .build()
            .create(AuthService::class.java)
    }

    // --- Cliente para citas (adjunta Authorization solo si la sesión es válida) ---
    fun appointments(context: Context, baseUrl: String = "https://crm.solbintec.com/"): AppointmentService {
        val tokenProvider = TokenProvider(context.applicationContext)

        val authInterceptor = AuthInterceptor(tokenProvider)

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization") // evita loguear el token
        }

        val ok = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // <-- usa tu clase aquí
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson)) // usa el mismo gson que en auth()
            .client(ok)
            .build()
            .create(AppointmentService::class.java)
    }
}