package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedules(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getSchedulesByDay(day: Int): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleItem>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleItem)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleItem)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("UPDATE schedules SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, enabled: Boolean)

    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()

    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getScheduleCount(): Int

    @Query("SELECT * FROM schedules WHERE reminderEnabled = 1")
    suspend fun getActiveSchedulesList(): List<ScheduleItem>
}
