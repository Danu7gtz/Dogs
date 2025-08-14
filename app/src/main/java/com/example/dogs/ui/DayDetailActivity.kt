package com.example.dogs.ui


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogs.R
import java.time.Instant
import java.time.ZoneId


import android.widget.TextView

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DayDetailActivity : BaseAgendaActivity() {

    private lateinit var adapter: AppointmentAdapter
    private var dayStart: Long = 0L
    private var dayEnd: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_detail)

        // Toolbar
        val tb = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Extras
        dayStart = intent.getLongExtra("dayStart", 0L)
        dayEnd = intent.getLongExtra("dayEnd", 0L)

        // Título
        val title = findViewById<TextView>(R.id.tvDay)
        val localDate = Instant.ofEpochMilli(dayStart).atZone(zoneMx).toLocalDate()
        title.text = "Citas del ${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"

        // Recycler + adapter
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAppointmentsDay)
        adapter = AppointmentAdapter(
            onEdit = { editAppointment(it, defaultDayStartMillis = dayStart) },
            onDelete = { vm.delete(it.id) },
            onCall = { a ->
                val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${a.phone}"))
                startActivity(i)
            }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv.adapter = adapter

        // Observa y filtra
        vm.appointments.observe(this) { list ->
            val dayList = list.filter { ap ->
                val s = ap.start.atZone(zoneMx).toInstant().toEpochMilli()
                val e = ap.end.atZone(zoneMx).toInstant().toEpochMilli()
                (s in dayStart until dayEnd) || (e in dayStart until dayEnd) || (s <= dayStart && e >= dayEnd)
            }.sortedBy { it.start }
            adapter.submitList(dayList)
        }
        vm.init()

        // FAB: PRUEBA de click + Abrir diálogo
        val fab = findViewById<FloatingActionButton>(R.id.fabAddDay)
        fab.setOnClickListener {
            // Quita este Toast luego: es para verificar que el click llega
            android.widget.Toast.makeText(this, "Agregar cita…", android.widget.Toast.LENGTH_SHORT).show()
            createOrEditDialog(existing = null, defaultDayStartMillis = dayStart)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
    }
}

