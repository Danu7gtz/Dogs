package com.example.dogs.data.mappers

import com.example.dogs.data.remote.CreateUpdateAppointmentBody
import com.example.dogs.domain.Appointment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.example.dogs.data.remote.AppointmentDto



private val ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun AppointmentDto.toDomain(): Appointment = Appointment(
    id = uuid,
    clientName = nombre_cliente,
    address = direccion,
    phone = "",
    start = LocalDateTime.parse(fecha_cita, ISO),
    end   = LocalDateTime.parse(fecha_cita, ISO).plusMinutes(60),
    notes = ""
)

fun Appointment.toBody(): CreateUpdateAppointmentBody =
    CreateUpdateAppointmentBody(
        nombre_cliente = clientName,
        nombre_mascota = notes.ifBlank { "Mascota" },
        direccion = address,
        fecha_cita = start.format(ISO),
        notas = if (notes.isBlank()) null else notes
    )