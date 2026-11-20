package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.TumbaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: "TUMBA NOTES"
        val noteContent = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TUMBA Notes Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled note reminders in TUMBA NOTES"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTE_ID, noteId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            openIntent,
            flags
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(noteTitle.ifBlank { "TUMBA Reminder" })
            .setContentText(noteContent.ifBlank { "Нагадування про нотатку" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(noteContent.ifBlank { "Нагадування про нотатку" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification,
                "Відкрити",
                pendingIntent
            )
            .build()

        notificationManager.notify(noteId.hashCode(), notification)

        // The reminder has been delivered: clear it so the note does not keep showing
        // a stale "active" reminder forever.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = TumbaDatabase.getDatabase(context, this)
                db.noteDao().updateReminder(noteId, null)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "tumba_reminders_channel"
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_NOTE_TITLE = "extra_note_title"
        const val EXTRA_NOTE_CONTENT = "extra_note_content"
    }
}
