package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val description: String = "",
    val dueDateTimeMillis: Long,
    val reminderMinutesBefore: Int = 60, // 0 = on time, 60 = 1h, 120 = 2h, 1440 = 1d, -1 = disabled
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val priority: String = "SEDANG", // RENDAH, SEDANG, TINGGI
    val createdAt: Long = System.currentTimeMillis()
)
