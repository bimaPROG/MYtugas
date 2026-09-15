package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(NotificationHelper.EXTRA_TYPE) ?: return

        when (type) {
            NotificationHelper.TYPE_TASK -> {
                val taskId = intent.getLongExtra(NotificationHelper.EXTRA_ID, -1L)
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Tugas Sekolah"
                val subject = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT) ?: "Mata Pelajaran"
                val desc = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION) ?: ""

                if (taskId != -1L) {
                    NotificationHelper.showTaskNotification(context, taskId, title, subject, desc)
                }
            }
            NotificationHelper.TYPE_SCHEDULE -> {
                val scheduleId = intent.getLongExtra(NotificationHelper.EXTRA_ID, -1L)
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Jadwal Pelajaran"
                val subject = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT) ?: "Mata Pelajaran"
                val room = intent.getStringExtra(NotificationHelper.EXTRA_ROOM) ?: ""
                val time = intent.getStringExtra(NotificationHelper.EXTRA_TIME) ?: ""

                if (scheduleId != -1L) {
                    NotificationHelper.showScheduleNotification(context, scheduleId, title, subject, room, time)
                }
            }
        }
    }
}
