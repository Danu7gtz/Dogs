package com.example.dogs.data.remote

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginUser(
    val uuid: String,
    val name: String,
    val email: String
)

data class LoginResponse(
    val user: LoginUser,
    val token: String
)

interface AuthService {
    // POST https://crm.solbintec.com/api/login?email=...&password=...
    @POST("api/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<LoginResponse>
}