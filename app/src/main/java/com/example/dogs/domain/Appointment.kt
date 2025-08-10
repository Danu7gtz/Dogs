package com.example.dogs.domain

import java.time.LocalDateTime
import java.util.UUID

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    var clientName: String,
    var address: String,
    var phone: String,
    var start: LocalDateTime,
    var end: LocalDateTime,
    var notes: String = ""
)
