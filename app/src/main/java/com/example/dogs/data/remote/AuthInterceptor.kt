package com.example.dogs.data.remote

import com.example.dogs.data.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        if (tokenProvider.isSessionValid()) {
            tokenProvider.getRawToken()?.let { builder.header("Authorization", "Bearer $it") }
        }

        val response = chain.proceed(builder.build())
        // No limpies aquí; deja que la UI decida en 401
        return response
    }
}