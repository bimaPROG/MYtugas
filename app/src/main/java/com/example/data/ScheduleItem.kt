package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dayOfWeek: Int, // 1=Senin, 2=Selasa, 3=Rabu, 4=Kamis, 5=Jumat, 6=Sabtu, 7=Minggu
    val subjectCode: String,
    val subjectName: String,
    val startTime: String, // e.g. "07:30"
    val endTime: String,   // e.g. "09:00"
    val roomOrTeacher: String = "",
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15
) {
    companion object {
        fun getDayName(day: Int): String {
            return when (day) {
                1 -> "Senin"
                2 -> "Selasa"
                3 -> "Rabu"
                4 -> "Kamis"
                5 -> "Jumat"
                6 -> "Sabtu"
                7 -> "Minggu"
                else -> "Senin"
            }
        }
    }
}
