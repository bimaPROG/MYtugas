package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_ID, -1L)
        val notifId = intent.getIntExtra("extra_notif_id", taskId.toInt())

        when (intent.action) {
            NotificationHelper.ACTION_MARK_DONE -> {
                if (taskId != -1L) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            db.taskDao().updateCompletionStatus(taskId, true, System.currentTimeMillis())
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            NotificationHelper.ACTION_DISMISS_ALARM -> {
                NotificationManagerCompat.from(context).cancel(notifId)
            }
            NotificationHelper.ACTION_SNOOZE -> {
                NotificationManagerCompat.from(context).cancel(notifId)
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Tugas Sekolah"
                val subject = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT) ?: "Mata Pelajaran"
                val desc = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION) ?: ""
                NotificationHelper.scheduleSnoozeReminder(context, taskId, title, subject, desc, minutes = 10)
            }
        }
    }
}
