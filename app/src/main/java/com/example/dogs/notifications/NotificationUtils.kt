package com.example.dogs.notifications


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dogs.R

object NotificationUtils {
    const val CHANNEL_ID = "new_appointment_channel"

    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Nuevas citas", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifica cuando se registra una nueva cita" }
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    fun notifyNewAppointment(ctx: Context, text: String) {
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nueva cita registrada")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(ctx).notify((0..99999).random(), builder.build())
    }
}
