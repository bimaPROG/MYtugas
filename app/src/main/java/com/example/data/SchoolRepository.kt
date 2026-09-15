package com.example.data

import kotlinx.coroutines.flow.Flow

class SchoolRepository(
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val allSchedules: Flow<List<ScheduleItem>> = scheduleDao.getAllSchedules()

    fun getSchedulesByDay(day: Int): Flow<List<ScheduleItem>> = scheduleDao.getSchedulesByDay(day)

    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
    suspend fun toggleTaskCompleted(task: TaskItem) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.updateCompletionStatus(task.id, newStatus, completedAt)
    }

    suspend fun insertSchedule(schedule: ScheduleItem): Long = scheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleItem) = scheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleItem) = scheduleDao.deleteSchedule(schedule)
    suspend fun deleteScheduleById(id: Long) = scheduleDao.deleteScheduleById(id)
    suspend fun toggleScheduleReminder(id: Long, enabled: Boolean) {
        scheduleDao.updateReminderStatus(id, enabled)
    }

    suspend fun resetToDefaultSchedules() {
        scheduleDao.deleteAllSchedules()
        scheduleDao.insertAll(getDefaultSchedules())
    }

    fun getDefaultSchedules(): List<ScheduleItem> = listOf(
        // === SENIN ===
        // Pelajaran 1: B. Inggris (06:30 - 07:50)
        ScheduleItem(dayOfWeek = 1, subjectCode = "B.ING", subjectName = "B. Inggris", startTime = "06:30", endTime = "07:50", roomOrTeacher = "Jam Ke-1"),
        // Pelajaran 2: KIK (07:50 - 09:10)
        ScheduleItem(dayOfWeek = 1, subjectCode = "KIK", subjectName = "KIK (Kewirausahaan)", startTime = "07:50", endTime = "09:10", roomOrTeacher = "Jam Ke-2"),
        // Pelajaran 3: PKN (09:40 - 11:00)
        ScheduleItem(dayOfWeek = 1, subjectCode = "PKN", subjectName = "PKN (Pancasila & Kewarganegaraan)", startTime = "09:40", endTime = "11:00", roomOrTeacher = "Jam Ke-3"),
        // Pelajaran 4: Peweb (11:00 - 12:20)
        ScheduleItem(dayOfWeek = 1, subjectCode = "PEWEB", subjectName = "Peweb (Pemrograman Web)", startTime = "11:00", endTime = "12:20", roomOrTeacher = "Jam Ke-4"),
        // Pelajaran 5: PPB (12:20 - 13:40)
        ScheduleItem(dayOfWeek = 1, subjectCode = "PPB", subjectName = "PPB (Perangkat Bergerak)", startTime = "12:20", endTime = "13:40", roomOrTeacher = "Jam Ke-5"),

        // === SELASA ===
        // Pelajaran 1: KIK (06:30 - 07:50)
        ScheduleItem(dayOfWeek = 2, subjectCode = "KIK", subjectName = "KIK (Kewirausahaan)", startTime = "06:30", endTime = "07:50", roomOrTeacher = "Jam Ke-1"),
        // Pelajaran 2: PPB (07:50 - 09:10)
        ScheduleItem(dayOfWeek = 2, subjectCode = "PPB", subjectName = "PPB (Perangkat Bergerak)", startTime = "07:50", endTime = "09:10", roomOrTeacher = "Jam Ke-2"),
        // Pelajaran 3: Basdat (09:40 - 11:00)
        ScheduleItem(dayOfWeek = 2, subjectCode = "BASDAT", subjectName = "Basdat (Basis Data)", startTime = "09:40", endTime = "11:00", roomOrTeacher = "Jam Ke-3"),
        // Pelajaran 4: B. Jepang (11:00 - 12:20)
        ScheduleItem(dayOfWeek = 2, subjectCode = "B.JPN", subjectName = "B. Jepang", startTime = "11:00", endTime = "12:20", roomOrTeacher = "Jam Ke-4"),
        // Pelajaran 5: PBGTM (12:20 - 13:40)
        ScheduleItem(dayOfWeek = 2, subjectCode = "PBGTM", subjectName = "PBGTM", startTime = "12:20", endTime = "13:40", roomOrTeacher = "Jam Ke-5"),

        // === RABU ===
        // Pelajaran 1: B. Sunda (06:30 - 07:50)
        ScheduleItem(dayOfWeek = 3, subjectCode = "B.SND", subjectName = "B. Sunda", startTime = "06:30", endTime = "07:50", roomOrTeacher = "Jam Ke-1"),
        // Pelajaran 2: B. Inggris (07:50 - 09:10)
        ScheduleItem(dayOfWeek = 3, subjectCode = "B.ING", subjectName = "B. Inggris", startTime = "07:50", endTime = "09:10", roomOrTeacher = "Jam Ke-2"),
        // Pelajaran 3: Basdat (09:40 - 11:00)
        ScheduleItem(dayOfWeek = 3, subjectCode = "BASDAT", subjectName = "Basdat (Basis Data)", startTime = "09:40", endTime = "11:00", roomOrTeacher = "Jam Ke-3"),
        // Pelajaran 4: KKA (11:00 - 12:20)
        ScheduleItem(dayOfWeek = 3, subjectCode = "KKA", subjectName = "KKA (Koding & AI)", startTime = "11:00", endTime = "12:20", roomOrTeacher = "Jam Ke-4"),
        // Pelajaran 5: PBGTM (12:20 - 13:40)
        ScheduleItem(dayOfWeek = 3, subjectCode = "PBGTM", subjectName = "PBGTM", startTime = "12:20", endTime = "13:40", roomOrTeacher = "Jam Ke-5"),

        // === KAMIS ===
        // Pelajaran 1: PEWEB (06:30 - 07:50)
        ScheduleItem(dayOfWeek = 4, subjectCode = "PEWEB", subjectName = "Peweb (Pemrograman Web)", startTime = "06:30", endTime = "07:50", roomOrTeacher = "Jam Ke-1"),
        // Pelajaran 2: MTK (07:50 - 09:50)
        ScheduleItem(dayOfWeek = 4, subjectCode = "MTK", subjectName = "MTK (Matematika)", startTime = "07:50", endTime = "09:50", roomOrTeacher = "Jam Ke-2"),
        // Pelajaran 3: B. Indo (09:50 - 12:20)
        ScheduleItem(dayOfWeek = 4, subjectCode = "B.INDO", subjectName = "B. Indo (Bahasa Indonesia)", startTime = "09:50", endTime = "12:20", roomOrTeacher = "Jam Ke-3"),
        // Pelajaran 4: PABP (12:20 - 13:40)
        ScheduleItem(dayOfWeek = 4, subjectCode = "PABP", subjectName = "PABP (Pendidikan Agama)", startTime = "12:20", endTime = "13:40", roomOrTeacher = "Jam Ke-4"),

        // === JUMAT ===
        // Pelajaran 1: PJOK (06:30 - 07:10)
        ScheduleItem(dayOfWeek = 5, subjectCode = "PJOK", subjectName = "PJOK (Olahraga)", startTime = "06:30", endTime = "07:10", roomOrTeacher = "Jam Ke-1"),
        // Pelajaran 2: PEWEB (07:50 - 09:10)
        ScheduleItem(dayOfWeek = 5, subjectCode = "PEWEB", subjectName = "Peweb (Pemrograman Web)", startTime = "07:50", endTime = "09:10", roomOrTeacher = "Jam Ke-2"),
        // Pelajaran 3: SJ (09:10 - 10:30)
        ScheduleItem(dayOfWeek = 5, subjectCode = "SJ", subjectName = "SJ (Sejarah)", startTime = "09:10", endTime = "10:30", roomOrTeacher = "Jam Ke-3")
    )

    suspend fun populateDefaultDataIfEmpty() {
        val activeTasks = taskDao.getActiveTasksList()
        val scheduleCount = scheduleDao.getScheduleCount()

        if (activeTasks.isEmpty()) {
            val now = System.currentTimeMillis()
            val hourMillis = 3600_000L
            val dayMillis = 24 * hourMillis

            // Add useful sample tasks with the requested subjects
            val sampleTasks = listOf(
                TaskItem(
                    title = "Membuat Form Input Biodata Mahasiswa",
                    subjectCode = "PEWEB",
                    subjectName = "Peweb (Pemrograman Web)",
                    description = "Gunakan tag HTML form, input text, select option, dan validasi sederhana JavaScript.",
                    dueDateTimeMillis = now + (2 * dayMillis) + (4 * hourMillis),
                    reminderMinutesBefore = 120,
                    priority = "TINGGI"
                ),
                TaskItem(
                    title = "Desain Tampilan UI Aplikasi Android",
                    subjectCode = "PPB",
                    subjectName = "PPB (Perangkat Bergerak)",
                    description = "Buat prototipe antarmuka menggunakan Jetpack Compose Layout Scaffold dan LazyColumn.",
                    dueDateTimeMillis = now + (3 * dayMillis),
                    reminderMinutesBefore = 60,
                    priority = "TINGGI"
                ),
                TaskItem(
                    title = "Latihan Soal Integral dan Matriks",
                    subjectCode = "MTK",
                    subjectName = "MTK (Matematika)",
                    description = "Kerjakan halaman 45 nomor 1 sampai 10 di buku latihan.",
                    dueDateTimeMillis = now + (1 * dayMillis) + (6 * hourMillis),
                    reminderMinutesBefore = 60,
                    priority = "SEDANG"
                ),
                TaskItem(
                    title = "Query Normalisasi Database Toko Online",
                    subjectCode = "BASDAT",
                    subjectName = "Basdat (Basis Data)",
                    description = "Buat query DDL & DML, relasi tabel 1NF sampai 3NF dengan foreign key.",
                    dueDateTimeMillis = now + (4 * dayMillis),
                    reminderMinutesBefore = 1440,
                    priority = "SEDANG"
                ),
                TaskItem(
                    title = "Menulis Percakapan Perkenalan (Jikoshoukai)",
                    subjectCode = "B.JPN",
                    subjectName = "B. Jepang",
                    description = "Tulis teks perkenalan diri dengan huruf Hiragana dan artinya.",
                    dueDateTimeMillis = now + (5 * dayMillis),
                    reminderMinutesBefore = 60,
                    priority = "RENDAH"
                ),
                TaskItem(
                    title = "Menyusun Proposal Usaha Produk Kreatif",
                    subjectCode = "KIK",
                    subjectName = "KIK (Kewirausahaan)",
                    description = "Analisis SWOT dan estimasi modal awal kelompok.",
                    dueDateTimeMillis = now + (6 * dayMillis),
                    reminderMinutesBefore = 1440,
                    priority = "SEDANG"
                )
            )

            sampleTasks.forEach { taskDao.insertTask(it) }
        }

        // Check if schedules need to be refreshed to this new timetable
        if (scheduleCount == 0 || scheduleCount == 15) {
            scheduleDao.deleteAllSchedules()
            scheduleDao.insertAll(getDefaultSchedules())
        }
    }
}
