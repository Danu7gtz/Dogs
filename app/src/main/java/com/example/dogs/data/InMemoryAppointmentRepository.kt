package com.example.dogs.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dogs.domain.Appointment
import java.time.LocalDate
import java.time.LocalDateTime

object InMemoryAppointmentRepository {
    private val _data = MutableLiveData<List<Appointment>>(seed())
    val data: LiveData<List<Appointment>> get() = _data

    fun add(a: Appointment): Result<Unit> {
        val list = _data.value.orEmpty()
        if (conflicts(a, list)) return Result.failure(IllegalStateException("Horario no disponible"))
        _data.value = list + a
        return Result.success(Unit)
    }

    fun update(a: Appointment): Result<Unit> {
        val list = _data.value.orEmpty()
        if (conflicts(a, list.filter { it.id != a.id })) return Result.failure(IllegalStateException("Horario no disponible"))
        _data.value = list.map { if (it.id == a.id) a else it }
        return Result.success(Unit)
    }

    fun delete(id: String) {
        _data.value = _data.value.orEmpty().filterNot { it.id == id }
    }

    private fun conflicts(c: Appointment, list: List<Appointment>): Boolean =
        list.any { e -> c.start.isBefore(e.end) && c.end.isAfter(e.start) }

    private fun seed(): List<Appointment> {
        val today = LocalDate.now()
        fun t(h: Int, m: Int = 0): LocalDateTime = today.atTime(h, m)
        return listOf(
            Appointment(clientName="Luna", address="Calle 123", phone="555-111-2222", start=t(9), end=t(10), notes="Nerviosa"),
            Appointment(clientName="Max", address="Av. Perros 45", phone="555-333-4444", start=t(11,30), end=t(12,30), notes="Collar azul")
        )
    }
}
