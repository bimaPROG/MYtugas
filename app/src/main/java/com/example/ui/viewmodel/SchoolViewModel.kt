package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SchoolRepository(db.taskDao(), db.scheduleDao())
        NotificationHelper.createNotificationChannels(application)

        // Seed initial data if empty
        viewModelScope.launch {
            repository.populateDefaultDataIfEmpty()
        }
    }

    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleItem>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectFilter = MutableStateFlow<String?>(null)
    val selectedSubjectFilter: StateFlow<String?> = _selectedSubjectFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("SEMUA") // SEMUA, AKTIF, SELESAI
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Default to current day (1=Senin..7=Minggu)
    private val currentDayIndex: Int = run {
        val cal = Calendar.getInstance()
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private val _selectedDay = MutableStateFlow(currentDayIndex)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Filtered tasks flow
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        allTasks,
        _selectedSubjectFilter,
        _selectedStatusFilter,
        _searchQuery
    ) { tasks, subjectFilter, statusFilter, query ->
        tasks.filter { task ->
            val matchSubject = subjectFilter == null || task.subjectCode.equals(subjectFilter, ignoreCase = true)
            val matchStatus = when (statusFilter) {
                "AKTIF" -> !task.isCompleted
                "SELESAI" -> task.isCompleted
                else -> true
            }
            val matchQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.subjectName.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)

            matchSubject && matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Schedules filtered by selected day
    val schedulesForSelectedDay: StateFlow<List<ScheduleItem>> = combine(
        allSchedules,
        _selectedDay
    ) { schedules, day ->
        schedules.filter { it.dayOfWeek == day }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary stats
    val taskStats = allTasks.map { tasks ->
        val now = System.currentTimeMillis()
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val active = total - completed
        val urgent = tasks.count { !it.isCompleted && it.dueDateTimeMillis in now..(now + 24 * 3600 * 1000L) }
        val overdue = tasks.count { !it.isCompleted && it.dueDateTimeMillis < now }
        TaskStats(total = total, active = active, completed = completed, urgent = urgent, overdue = overdue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    fun setSubjectFilter(code: String?) {
        _selectedSubjectFilter.value = code
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
    }

    fun addTask(task: TaskItem) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            val insertedTask = task.copy(id = id)
            NotificationHelper.scheduleTaskReminder(getApplication(), insertedTask)
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
            if (task.isCompleted) {
                NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else {
                NotificationHelper.scheduleTaskReminder(getApplication(), task)
            }
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            if (!task.isCompleted) { // will become completed
                NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else {
                NotificationHelper.scheduleTaskReminder(getApplication(), task.copy(isCompleted = false))
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun addSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            val id = repository.insertSchedule(schedule)
            val insertedSchedule = schedule.copy(id = id)
            NotificationHelper.scheduleScheduleReminder(getApplication(), insertedSchedule)
        }
    }

    fun updateSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
            if (schedule.reminderEnabled) {
                NotificationHelper.scheduleScheduleReminder(getApplication(), schedule)
            } else {
                NotificationHelper.cancelScheduleReminder(getApplication(), schedule.id)
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            NotificationHelper.cancelScheduleReminder(getApplication(), schedule.id)
            repository.deleteSchedule(schedule)
        }
    }

    fun toggleScheduleReminder(schedule: ScheduleItem) {
        viewModelScope.launch {
            val newStatus = !schedule.reminderEnabled
            repository.toggleScheduleReminder(schedule.id, newStatus)
            if (newStatus) {
                NotificationHelper.scheduleScheduleReminder(getApplication(), schedule.copy(reminderEnabled = true))
            } else {
                NotificationHelper.cancelScheduleReminder(getApplication(), schedule.id)
            }
        }
    }

    fun resetScheduleToDefault() {
        viewModelScope.launch {
            repository.resetToDefaultSchedules()
            // Reschedule notifications for active reminders
            val schedules = repository.getDefaultSchedules()
            schedules.forEach {
                if (it.reminderEnabled) {
                    NotificationHelper.scheduleScheduleReminder(getApplication(), it)
                }
            }
        }
    }

    fun sendTestNotification() {
        NotificationHelper.sendTestNotification(getApplication())
    }
}

data class TaskStats(
    val total: Int = 0,
    val active: Int = 0,
    val completed: Int = 0,
    val urgent: Int = 0,
    val overdue: Int = 0
)
