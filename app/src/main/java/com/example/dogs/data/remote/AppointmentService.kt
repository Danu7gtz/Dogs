package com.example.dogs.data.remote

import retrofit2.Response
import retrofit2.http.*



data class AppointmentDto(
    val id: Int,
    val uuid: String,
    val nombre_cliente: String,
    val nombre_mascota: String,
    val direccion: String,
    val fecha_cita: String,     // "2025-08-15 15:20:20"
    val created_at: String?,
    val updated_at: String?
)

data class CreateUpdateAppointmentBody(
    val nombre_cliente: String,
    val nombre_mascota: String,
    val direccion: String,
    val fecha_cita: String,
    val notas: String? = null
)

interface AppointmentService {
    // GET /citas
    @GET("api/citas")
    suspend fun listAppointments(): Response<List<AppointmentDto>>

    // POST /citas-add
    @POST("api/citas-add")
    suspend fun createAppointment(
        @Body body: CreateUpdateAppointmentBody
    ): Response<AppointmentDto>

    // GET /citas-view/{id}
    @GET("api/citas-view/{id}")
    suspend fun getAppointment(@Path("id") id: String): Response<AppointmentDto>

    // PUT /citas-update/{id}
    @PUT("api/citas-update/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String,
        @Body body: CreateUpdateAppointmentBody
    ): Response<AppointmentDto>

    // DELETE /citas-delete/{id}
    @DELETE("api/citas-delete/{id}")
    suspend fun deleteAppointment(@Path("id") id: String): Response<Unit>

}