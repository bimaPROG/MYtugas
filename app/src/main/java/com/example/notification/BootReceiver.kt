package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    // Reschedule active tasks
                    val activeTasks = db.taskDao().getActiveTasksList()
                    activeTasks.forEach { task ->
                        NotificationHelper.scheduleTaskReminder(context, task)
                    }

                    // Reschedule active schedules
                    val activeSchedules = db.scheduleDao().getActiveSchedulesList()
                    activeSchedules.forEach { schedule ->
                        NotificationHelper.scheduleScheduleReminder(context, schedule)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
