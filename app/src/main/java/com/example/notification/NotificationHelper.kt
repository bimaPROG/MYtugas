package com.example.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ScheduleItem
import com.example.data.TaskItem
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_TASKS_ID = "channel_tasks_deadline_v2"
    const val CHANNEL_SCHEDULES_ID = "channel_schedules_v2"

    const val EXTRA_TYPE = "extra_type"
    const val TYPE_TASK = "type_task"
    const val TYPE_SCHEDULE = "type_schedule"

    const val EXTRA_ID = "extra_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_SUBJECT = "extra_subject"
    const val EXTRA_DESCRIPTION = "extra_description"
    const val EXTRA_ROOM = "extra_room"
    const val EXTRA_TIME = "extra_time"

    const val ACTION_MARK_DONE = "com.example.ACTION_MARK_DONE"
    const val ACTION_DISMISS_ALARM = "com.example.ACTION_DISMISS_ALARM"
    const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val vibratePattern = longArrayOf(0, 800, 300, 800, 300, 800)

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS_ID,
                "Pengingat Tenggat Tugas (Alarm Kuat)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat tenggat tugas sekolah dengan dering alarm intensif"
                enableVibration(true)
                this.vibrationPattern = vibratePattern
                setSound(alarmSound, audioAttributes)
                setBypassDnd(true)
            }

            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULES_ID,
                "Pengingat Jadwal Pelajaran (Alarm Kuat)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat waktu mulai jadwal pelajaran sekolah"
                enableVibration(true)
                this.vibrationPattern = vibratePattern
                setSound(alarmSound, audioAttributes)
                setBypassDnd(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(scheduleChannel)
        }
    }

    fun scheduleTaskReminder(context: Context, task: TaskItem) {
        if (task.isCompleted || task.reminderMinutesBefore < 0) {
            cancelTaskReminder(context, task.id)
            return
        }

        val triggerTime = task.dueDateTimeMillis - (task.reminderMinutesBefore * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_TASK)
            putExtra(EXTRA_ID, task.id)
            putExtra(EXTRA_TITLE, task.title)
            putExtra(EXTRA_SUBJECT, task.subjectName)
            putExtra(EXTRA_DESCRIPTION, task.description)
            putExtra(EXTRA_TIME, task.dueDateTimeMillis)
        }

        val requestCode = (100000 + task.id).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun scheduleSnoozeReminder(
        context: Context,
        taskId: Long,
        title: String,
        subject: String,
        description: String,
        minutes: Int = 10
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_TASK)
            putExtra(EXTRA_ID, taskId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_DESCRIPTION, description)
        }
        val requestCode = (300000 + taskId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = (100000 + taskId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleScheduleReminder(context: Context, schedule: ScheduleItem) {
        if (!schedule.reminderEnabled) {
            cancelScheduleReminder(context, schedule.id)
            return
        }

        val parts = schedule.startTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, when (schedule.dayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            })
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -schedule.reminderMinutesBefore)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_SCHEDULE)
            putExtra(EXTRA_ID, schedule.id)
            putExtra(EXTRA_TITLE, "Jadwal Pelajaran: ${schedule.subjectName}")
            putExtra(EXTRA_SUBJECT, schedule.subjectName)
            putExtra(EXTRA_ROOM, schedule.roomOrTeacher)
            putExtra(EXTRA_TIME, "${schedule.startTime} - ${schedule.endTime}")
        }

        val requestCode = (200000 + schedule.id).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelScheduleReminder(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = (200000 + scheduleId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showTaskNotification(
        context: Context,
        taskId: Long,
        title: String,
        subject: String,
        description: String
    ) {
        createNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifId = taskId.toInt()

        // Action: Tandai Selesai
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_ID, taskId)
            putExtra("extra_notif_id", notifId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 50000,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Tunda 10 Menit (Snooze)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ID, taskId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra("extra_notif_id", notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 60000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Heningkan Alarm
        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra("extra_notif_id", notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 70000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (description.isNotBlank()) "$subject • $description" else "$subject • Segera selesaikan tugas sekarang!"

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_TASKS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ PENGINGAT TUGAS: $title")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🔔 TENGGAT WAKTU TUGAS!\n\nMata Pelajaran: $subject\nTugas: $title\n${if (description.isNotBlank()) "Keterangan: $description\n" else ""}⚠️ Notifikasi ini berdering & menetap sampai kamu merespon atau menandai selesai.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // Tetap berada di panel notifikasi, tidak hilang saat diswipe
            .setAutoCancel(false)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 800, 300, 800, 300, 800))
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "✅ Selesai", markDonePendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "⏰ Tunda 10 Mnt", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "🔕 Heningkan", dismissPendingIntent)

        val notification = builder.build()
        // FLAG_INSISTENT membuat audio dering/getaran terus berulang sampai notifikasi disentuh/dihentikan
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showScheduleNotification(
        context: Context,
        scheduleId: Long,
        title: String,
        subject: String,
        room: String,
        time: String
    ) {
        createNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifId = (scheduleId + 30000).toInt()
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra("extra_notif_id", notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 80000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Pelajaran $subject jam $time" + if (room.isNotBlank()) " di $room" else ""

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHEDULES_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🔔 $title")
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🔔 JADWAL MASUK KELAS!\n\nMata Pelajaran: $subject\nWaktu: $time\n${if (room.isNotBlank()) "Lokasi/Pengajar: $room\n" else ""}Siapkan buku dan perlengkapan pelajaran sekarang!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // Tetap berada di status bar
            .setAutoCancel(false)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 800, 300, 800, 300, 800))
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "🔕 Tutup Notifikasi", dismissPendingIntent)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendTestNotification(context: Context) {
        showTaskNotification(
            context = context,
            taskId = 99999L,
            title = "Contoh Tugas Pemrograman Web",
            subject = "Peweb (Pemrograman Web)",
            description = "Ini adalah pengingat pengujian intensif (Alarm terus-menerus). Notifikasi ini menetap dan memiliki tombol aksi!"
        )
    }
}
