package com.example.dogs.data

import com.example.dogs.data.remote.ApiClient
import com.example.dogs.data.remote.LoginResponse

class AuthRepository(
    private val baseUrl: String = "https://crm.solbintec.com/"
) {
    private val api = ApiClient.auth(baseUrl)

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val resp = api.login(email, password)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) Result.success(body)
                else Result.failure(IllegalStateException("Respuesta vacía del servidor"))
            } else {
                val err = resp.errorBody()?.string().orEmpty()
                Result.failure(IllegalStateException("HTTP ${resp.code()} ${resp.message()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}