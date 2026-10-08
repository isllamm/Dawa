package com.family.dawa.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.family.dawa.R
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.Slot
import com.family.dawa.ui.MainActivity
import com.family.dawa.presentation.caregiver.reminder.ReminderActivity
import java.io.File

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_DUE_ID = "dawa_channel_due"
        const val CHANNEL_MISSED_ID = "dawa_channel_missed"
        const val NOTIFICATION_DUE_ID = 9001
        const val NOTIFICATION_MISSED_ID = 9002
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.chime}")

            // Due channel (High importance, sound, vibration, bypass DND)
            val dueChannel = NotificationChannel(
                CHANNEL_DUE_ID,
                context.getString(R.string.channel_due_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_due_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 1000)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            // Missed channel
            val missedChannel = NotificationChannel(
                CHANNEL_MISSED_ID,
                context.getString(R.string.channel_missed_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_missed_desc)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(dueChannel)
            notificationManager.createNotificationChannel(missedChannel)
        }
    }

    fun showDueNotification(slot: Slot, items: List<DoseItem>) {
        val fullScreenIntent = Intent(context, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("slot_date", slot.date.toString())
            putExtra("slot_time", slot.timeMinutes)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            100,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val countText = if (items.size == 1) {
            "دواء واحد الآن"
        } else {
            "${ArabicFormatters.toArabicDigits(items.size)} أدوية الآن"
        }

        val medsSummary = items.joinToString(separator = " + ") { it.medicationName }

        val builder = NotificationCompat.Builder(context, CHANNEL_DUE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 دلوقتي معاد الدوا ($countText)")
            .setContentText(medsSummary)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true) // Stays until confirmed
            .setAutoCancel(false)

        // Show large primary photo if available
        val primaryPhotoPath = items.firstOrNull()?.primaryPhotoPath
        if (!primaryPhotoPath.isNullOrEmpty() && File(primaryPhotoPath).exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(primaryPhotoPath)
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setSummaryText(medsSummary)
                    )
                }
            } catch (_: Exception) {}
        }

        notificationManager.notify(NOTIFICATION_DUE_ID, builder.build())
    }

    fun cancelDueNotification() {
        notificationManager.cancel(NOTIFICATION_DUE_ID)
    }

    fun showMissedNotification(slot: Slot) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = ArabicFormatters.formatMinutesOfDay(slot.timeMinutes)

        val builder = NotificationCompat.Builder(context, CHANNEL_MISSED_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⛔ دواء متأخر")
            .setContentText("فات موعد دواء الساعة $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_MISSED_ID, builder.build())
    }
}
