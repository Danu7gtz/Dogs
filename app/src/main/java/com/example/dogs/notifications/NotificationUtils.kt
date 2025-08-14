package com.example.dogs.notifications


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.dogs.R
import com.example.dogs.ui.AgendaActivity

object NotificationUtils {
    const val CHANNEL_ID = "new_appointment_channel"
    private const val GROUP_APPTS = "group_new_appts"

    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Nuevas citas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica cuando se registra una nueva cita"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 120, 80, 120)
                setShowBadge(true)

                lightColor = Color.RED
                enableLights(true)
            }
            nm.createNotificationChannel(ch)
        }
    }

    fun notifyNewAppointment(ctx: Context, text: String) {
        val notifRed = ContextCompat.getColor(ctx, R.color.notif_red)

        val intent = Intent(ctx, AgendaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPI = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionPI = PendingIntent.getActivity(
            ctx, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nueva cita registrada")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(contentPI)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setColor(notifRed)
            .setColorized(true)
            .addAction(
                R.drawable.ic_notification, // icono peque blanco
                "Ver",
                actionPI
            )
            .setGroup(GROUP_APPTS)

        NotificationManagerCompat.from(ctx).apply {
            notify((0..99999).random(), builder.build())

            // Notificación resumen de grupo
            val summary = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nuevas citas")
                .setContentText("Tienes varias citas recientes")
                .setStyle(NotificationCompat.InboxStyle()
                    .addLine(text)
                    .setSummaryText("Agenda"))
                .setColor(notifRed)
                .setColorized(true)
                .setGroup(GROUP_APPTS)
                .setGroupSummary(true)
                .build()

            notify(100_000, summary)
        }
    }
}