package com.example.dogs.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dogs.data.mappers.toBody
import com.example.dogs.data.mappers.toDomain
import com.example.dogs.data.remote.ApiClient
import com.example.dogs.domain.Appointment

class AppointmentRepository(context: Context) {
    //commit 1

    private val api = ApiClient.appointments(context)

    private val _data = MutableLiveData<List<Appointment>>(emptyList())
    val data: LiveData<List<Appointment>> get() = _data

    suspend fun refresh(): Result<Unit> = runCatching {
        val resp = api.listAppointments()
        if (!resp.isSuccessful) error("HTTP ${resp.code()} ${resp.message()}")
        val list = resp.body().orEmpty().map { it.toDomain() }.sortedBy { it.start }
        _data.postValue(list)
    }

    // AppointmentRepository.kt
    suspend fun add(a: Appointment): Result<Appointment> = runCatching {
        val resp = api.createAppointment(a.toBody())
        if (!resp.isSuccessful) {
            error("HTTP ${resp.code()} ${resp.message()} ${resp.errorBody()?.string().orEmpty()}")
        }

        val created = resp.body()?.let { body ->
            try {
                body.toDomain()
            } catch (e: Throwable) {
                android.util.Log.e("API_MAPPER", "Fallo en toDomain() (add): ${e.message}", e)
                a
            }
        } ?: a

        _data.postValue((_data.value.orEmpty() + created).sortedBy { it.start })
        created
    }

    suspend fun update(a: Appointment): Result<Appointment> = runCatching {
        val resp = api.updateAppointment(a.id, a.toBody())
        if (!resp.isSuccessful) error("HTTP ${resp.code()} ${resp.message()} ${resp.errorBody()?.string().orEmpty()}")
        val updated = resp.body()!!.toDomain()
        _data.postValue(_data.value.orEmpty().map { if (it.id == a.id) updated else it }.sortedBy { it.start })
        updated
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        val resp = api.deleteAppointment(id)
        if (!resp.isSuccessful) error("HTTP ${resp.code()} ${resp.message()} ${resp.errorBody()?.string().orEmpty()}")
        _data.postValue(_data.value.orEmpty().filterNot { it.id == id })
    }
}