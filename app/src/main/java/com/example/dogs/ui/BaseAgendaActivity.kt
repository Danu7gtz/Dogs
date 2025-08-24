package com.example.dogs.ui


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dogs.R
import com.example.dogs.domain.Appointment
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import com.google.firebase.messaging.FirebaseMessaging

/** Clase base con utilidades compartidas entre AgendaActivity y DayDetailActivity */
abstract class BaseAgendaActivity : AppCompatActivity() {

    protected val vm: AgendaViewModel by viewModels()
    protected val fmtDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    protected val zoneMx: ZoneId = ZoneId.of("America/Mexico_City")

    /**
     * Abre el diálogo para crear/editar una cita.
     * @param existing cita existente (null = nueva)
     * @param defaultDayStartMillis día base para abrir los pickers (medianoche local)
     */


    protected fun createOrEditDialog(
        existing: Appointment? = null,
        defaultDayStartMillis: Long? = null
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_appointment, null)
        val etClient = dialogView.findViewById<android.widget.EditText>(R.id.etClient)
        val etAddress = dialogView.findViewById<android.widget.EditText>(R.id.etAddress)
        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etPhone)
        val btnStart = dialogView.findViewById<android.widget.Button>(R.id.btnStart)
        val btnEnd = dialogView.findViewById<android.widget.Button>(R.id.btnEnd)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etNotes)

        var start: LocalDateTime? = existing?.start
        var end: LocalDateTime? = existing?.end

        fun pickDateTime(onPicked: (LocalDateTime) -> Unit) {
            val base = Calendar.getInstance().apply {
                if (defaultDayStartMillis != null) {
                    timeInMillis = defaultDayStartMillis
                }
            }
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    TimePickerDialog(
                        this,
                        { _, hh, mm -> onPicked(LocalDateTime.of(y, m + 1, d, hh, mm)) },
                        base.get(Calendar.HOUR_OF_DAY),
                        base.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnStart.text = start?.format(fmtDate) ?: "Elegir inicio"
        btnEnd.text = end?.format(fmtDate) ?: "Elegir fin"

        btnStart.setOnClickListener { pickDateTime { dt -> start = dt; btnStart.text = dt.format(fmtDate) } }
        btnEnd.setOnClickListener { pickDateTime { dt -> end = dt; btnEnd.text = dt.format(fmtDate) } }

        existing?.let {
            etClient.setText(it.clientName)
            etAddress.setText(it.address)
            etPhone.setText(it.phone)
            etNotes.setText(it.notes)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Nueva cita" else "Editar cita")
            .setView(dialogView)
            .setPositiveButton("Guardar") { d, _ ->
                val client = etClient.text.toString().trim()
                val address = etAddress.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val notes = etNotes.text.toString().trim()
                if (client.isEmpty() || address.isEmpty() || phone.isEmpty() || start == null || end == null) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (!end!!.isAfter(start)) {
                    Toast.makeText(this, "Fin debe ser después de inicio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val appt = if (existing == null) {
                    Appointment(clientName = client, address = address, phone = phone,
                        start = start!!, end = end!!, notes = notes)
                } else {
                    existing.copy(clientName = client, address = address, phone = phone,
                        start = start!!, end = end!!, notes = notes)
                }

                if (existing == null) vm.add(appt) else vm.update(appt)
                d.dismiss()
            }
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .show()
    }

    fun editAppointment(a: Appointment, defaultDayStartMillis: Long? = null) {
        createOrEditDialog(a, defaultDayStartMillis)
    }
}
