package com.example.dogs.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dogs.R
import com.example.dogs.domain.Appointment
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentAdapter(
    private val onEdit: (Appointment) -> Unit,
    private val onDelete: (Appointment) -> Unit,
    private val onCall: ((Appointment) -> Unit)? = null
) : ListAdapter<Appointment, AppointmentAdapter.VH>(Diff()) {

    private val zoneMx = ZoneId.of("America/Mexico_City")
    private val niceDate = DateTimeFormatter.ofPattern("EEE d MMM", Locale("es", "MX"))
    private val niceTime = DateTimeFormatter.ofPattern("h:mm a", Locale("es", "MX"))

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvClienteMascota: TextView = v.findViewById(R.id.tvClienteMascota)
        val tvStatus: TextView = v.findViewById(R.id.tvStatus)
        val tvFechaRango: TextView = v.findViewById(R.id.tvFechaRango)
        val tvDireccion: TextView = v.findViewById(R.id.tvDireccion)
        val btnCall: ImageButton = v.findViewById(R.id.btnCall)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = getItem(position)

        h.tvClienteMascota.text = item.clientName

        // Fecha
        val startZ = item.start.atZone(zoneMx)
        val endZ = item.end.atZone(zoneMx)
        val fecha = startZ.format(niceDate).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es","MX")) else it.toString() }
        val rango = "${startZ.format(niceTime).lowercase()} - ${endZ.format(niceTime).lowercase()}"
        h.tvFechaRango.text = "$fecha  ·  $rango"

        // Dirección
        h.tvDireccion.text = item.address

        // Estado
        val isPast = endZ.toInstant().isBefore(java.time.Instant.now())
        if (isPast) {
            h.tvStatus.text = "Pasada"
            h.tvStatus.background.setTint(Color.parseColor("#E53935")) // rojo
        } else {
            h.tvStatus.text = "Próxima"
            h.tvStatus.background.setTint(Color.parseColor("#4CAF50")) // verde
        }

        // Acciones
        h.btnEdit.setOnClickListener { onEdit(item) }
        h.btnDelete.setOnClickListener { onDelete(item) }
        h.btnCall.setOnClickListener {
            onCall?.invoke(item) ?: run {
                val ctx = h.itemView.context
                val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
                ctx.startActivity(i)
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(old: Appointment, new: Appointment) = old.id == new.id
        override fun areContentsTheSame(old: Appointment, new: Appointment) = old == new
    }
}

