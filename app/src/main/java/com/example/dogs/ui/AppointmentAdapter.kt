package com.example.dogs.ui



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogs.databinding.ItemAppointmentBinding
import com.example.dogs.domain.Appointment
import java.time.format.DateTimeFormatter

class AppointmentAdapter(
    private var items: List<Appointment>,
    private val onEdit: (Appointment) -> Unit,
    private val onDelete: (Appointment) -> Unit
): RecyclerView.Adapter<AppointmentAdapter.VH>() {

    private val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

    inner class VH(val vb: ItemAppointmentBinding): RecyclerView.ViewHolder(vb.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.vb.tvClient.text = a.clientName
        holder.vb.tvAddress.text = a.address
        holder.vb.tvTime.text = "${a.start.format(fmt)} - ${a.end.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        holder.vb.btnEdit.setOnClickListener { onEdit(a) }
        holder.vb.btnDelete.setOnClickListener { onDelete(a) }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<Appointment>) {
        items = list
        notifyDataSetChanged()
    }
}
