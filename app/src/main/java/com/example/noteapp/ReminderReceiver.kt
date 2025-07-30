package com.example.noteapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.noteapp.ui.elements.fragments.NoteListFragment
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Reminder triggered at ${System.currentTimeMillis()} for note ID: ${intent.getStringExtra("NOTE_ID")}")
        val noteTitle = intent.getStringExtra("NOTE_TITLE") ?: "Ghi chú"
        val noteId = intent.getStringExtra("NOTE_ID") ?: "0"

        val notificationIntent = Intent(context, NoteListFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTE_ID", noteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, noteId.hashCode(), notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "note_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nhắc nhở ghi chú")
            .setContentText("Đã đến giờ nhắc nhở: $noteTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "note_channel",
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(noteId.hashCode(), notification)
    }
}