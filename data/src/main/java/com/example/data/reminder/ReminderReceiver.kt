package com.example.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.data.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_TITLE = "extra_title"
        private const val CHANNEL_ID = "reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        val defaultTitle = context.getString(R.string.data_reminder_notification_title)
        val title = intent.getStringExtra(EXTRA_TITLE)?.ifBlank { defaultTitle } ?: defaultTitle

        createChannel(context)

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra("noteId", noteId)
            }
        val contentIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // TODO: your own notification icon
            .setContentTitle(defaultTitle)
            .setContentText(title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(noteId.toInt(), notification)

    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.data_reminder_channel_name), NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.data_reminder_channel_desc) }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

}