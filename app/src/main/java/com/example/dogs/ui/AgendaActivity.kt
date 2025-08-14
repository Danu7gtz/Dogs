package com.example.dogs.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogs.R
import com.example.dogs.databinding.ActivityAgendaBinding
import com.example.dogs.domain.Appointment
import com.example.dogs.notifications.NotificationUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

import android.widget.CalendarView
import androidx.core.view.isVisible
import androidx.core.os.bundleOf




class AgendaActivity : BaseAgendaActivity() {

    private lateinit var vb: ActivityAgendaBinding
    private lateinit var adapter: AppointmentAdapter

    // Rango del día seleccionado (en millis, hora local)
    private var selectedStartMillis: Long = 0L
    private var selectedEndMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(vb.root)

        NotificationUtils.createChannel(this)

        // RecyclerView
        adapter = AppointmentAdapter(
            onEdit = { editAppointment(it, selectedStartMillis) },
            onDelete = { vm.delete(it.id) },
            onCall = { a ->
                val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${a.phone}"))
                startActivity(i)
            }
        )
        vb.rvAppointments.layoutManager = LinearLayoutManager(this)
        vb.rvAppointments.adapter = adapter
        vb.rvAppointments.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        // CalendarView
        vb.calendarView?.let { calView ->
            setSelectedDayBounds(calView.date)
            calView.setOnDateChangeListener { _: CalendarView, y: Int, m: Int, d: Int ->
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m)
                    set(Calendar.DAY_OF_MONTH, d)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                setSelectedDayBounds(c.timeInMillis)
                vm.appointments.value?.let { updateListForSelectedDay(it) }

                // 👉 abrir detalle como Activity separada
                openDayActivity(c.timeInMillis)
            }
        }

        vm.appointments.observe(this) { list -> updateListForSelectedDay(list) }
        vm.init()

        vb.btnAdd.setOnClickListener { createOrEditDialog(defaultDayStartMillis = selectedStartMillis) }
    }

    private fun updateListForSelectedDay(list: List<Appointment>) {
        val dayList = list.filter { ap ->
            val apStart = ap.start.atZone(zoneMx).toInstant().toEpochMilli()
            val apEnd = ap.end.atZone(zoneMx).toInstant().toEpochMilli()
            (apStart in selectedStartMillis until selectedEndMillis) ||
                    (apEnd in selectedStartMillis until selectedEndMillis) ||
                    (apStart <= selectedStartMillis && apEnd >= selectedEndMillis)
        }

        val now = Instant.now()
        val sorted = dayList.sortedWith(
            compareBy<Appointment> {
                it.end.atZone(zoneMx).toInstant().isBefore(now)
            }.thenBy { it.start }
        )
        adapter.submitList(sorted)
    }

    private fun setSelectedDayBounds(dayMillis: Long) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedStartMillis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        selectedEndMillis = cal.timeInMillis
    }

    private fun openDayActivity(dayStartMillis: Long) {
        val c = Calendar.getInstance().apply {
            timeInMillis = dayStartMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = c.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, 1)
        val end = c.timeInMillis

        val i = Intent(this, DayDetailActivity::class.java).apply {
            putExtra("dayStart", start)
            putExtra("dayEnd", end)
        }
        startActivity(i)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }
}


