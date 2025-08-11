package com.example.dogs.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogs.R
import com.example.dogs.databinding.ActivityAgendaBinding
import com.example.dogs.domain.Appointment
import com.example.dogs.notifications.NotificationUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AgendaActivity : AppCompatActivity() {

    private lateinit var vb: ActivityAgendaBinding
    private val vm: AgendaViewModel by viewModels()

    private lateinit var adapter: AppointmentAdapter
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // AgendaActivity.kt (onCreate)
        NotificationUtils.createChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Recycler + Adapter (DOMINIO)
        adapter = AppointmentAdapter(
            items = emptyList(),
            onEdit = { editAppointment(it) },
            onDelete = { vm.delete(it.id) }
        )
        vb.rvAppointments.layoutManager = LinearLayoutManager(this)
        vb.rvAppointments.adapter = adapter

        // Observa lista de dominio
        vm.appointments.observe(this) { list ->
            adapter.submit(list.sortedBy { it.start })
        }

        // Carga inicial
        vm.init()

        // Crear nueva cita
        vb.btnAdd.setOnClickListener { createOrEditDialog() }
    }

    private fun editAppointment(a: Appointment) = createOrEditDialog(a)

    private fun createOrEditDialog(existing: Appointment? = null) {
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
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, hh, mm ->
                    onPicked(LocalDateTime.of(y, m + 1, d, hh, mm))
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnStart.text = start?.format(fmtDate) ?: "Elegir inicio"
        btnEnd.text = end?.format(fmtDate) ?: "Elegir fin"

        btnStart.setOnClickListener {
            pickDateTime { dt -> start = dt; btnStart.text = dt.format(fmtDate) }
        }
        btnEnd.setOnClickListener {
            pickDateTime { dt -> end = dt; btnEnd.text = dt.format(fmtDate) }
        }

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
                if (client.isEmpty() || address.isEmpty() ||  phone.isEmpty() ||  start == null ||  end == null) {
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
}
