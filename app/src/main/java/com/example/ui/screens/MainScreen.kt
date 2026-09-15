package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ScheduleItem
import com.example.data.SchoolSubjects
import com.example.data.Subject
import com.example.data.TaskItem
import com.example.ui.components.*
import com.example.ui.viewmodel.SchoolViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val schedulesForDay by viewModel.schedulesForSelectedDay.collectAsStateWithLifecycle()
    val allSchedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val stats by viewModel.taskStats.collectAsStateWithLifecycle()
    val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) } // 0=Tugas, 1=Jadwal, 2=Mata Pelajaran
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog States
    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }

    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ScheduleItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Cari tugas atau mata pelajaran...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field")
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Logo MYtugas",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .testTag("app_logo_image")
                            )
                            Column {
                                Text(
                                    text = "MYtugas",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Pengingat Tugas & Jadwal Pelajaran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) viewModel.setSearchQuery("")
                        },
                        modifier = Modifier.testTag("toggle_search_btn")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Tutup Pencarian" else "Cari"
                        )
                    }

                    // Test Notification Button
                    IconButton(
                        onClick = {
                            viewModel.sendTestNotification()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Notifikasi pengingat contoh telah dikirim!")
                            }
                        },
                        modifier = Modifier.testTag("test_notification_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Uji Notifikasi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        BadgedBox(badge = {
                            if (stats.active > 0) {
                                Badge { Text(stats.active.toString()) }
                            }
                        }) {
                            Icon(Icons.Default.Assignment, contentDescription = "Tugas")
                        }
                    },
                    label = { Text("Tugas") },
                    modifier = Modifier.testTag("nav_item_tasks")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Jadwal") },
                    label = { Text("Jadwal") },
                    modifier = Modifier.testTag("nav_item_schedule")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Mata Pelajaran") },
                    label = { Text("Mapel (15)") },
                    modifier = Modifier.testTag("nav_item_subjects")
                )
            }
        },
        floatingActionButton = {
            when (currentTab) {
                0 -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            taskToEdit = null
                            showTaskDialog = true
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Tugas") },
                        modifier = Modifier.testTag("fab_add_task")
                    )
                }
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            scheduleToEdit = null
                            showScheduleDialog = true
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Jadwal") },
                        modifier = Modifier.testTag("fab_add_schedule")
                    )
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Notification permission banner if required
            NotificationPermissionHandler()

            // Active Tab Content
            when (currentTab) {
                0 -> TaskScreen(
                    tasks = tasks,
                    stats = stats,
                    selectedSubject = selectedSubjectFilter,
                    selectedStatus = selectedStatusFilter,
                    onSelectSubject = { viewModel.setSubjectFilter(it) },
                    onSelectStatus = { viewModel.setStatusFilter(it) },
                    onToggleCompleted = { task ->
                        viewModel.toggleTaskCompleted(task)
                        coroutineScope.launch {
                            val msg = if (!task.isCompleted) "Tugas '${task.title}' ditandai selesai!" else "Tugas dikembalikan ke aktif."
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    onEditTask = { task ->
                        taskToEdit = task
                        showTaskDialog = true
                    },
                    onDeleteTask = { task ->
                        viewModel.deleteTask(task)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Tugas '${task.title}' telah dihapus.")
                        }
                    }
                )

                1 -> ScheduleScreen(
                    schedules = schedulesForDay,
                    selectedDay = selectedDay,
                    onSelectDay = { viewModel.setSelectedDay(it) },
                    onToggleReminder = { schedule ->
                        viewModel.toggleScheduleReminder(schedule)
                        coroutineScope.launch {
                            val msg = if (!schedule.reminderEnabled) "Pengingat jadwal diaktifkan!" else "Pengingat jadwal dinonaktifkan."
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    onEditSchedule = { schedule ->
                        scheduleToEdit = schedule
                        showScheduleDialog = true
                    },
                    onDeleteSchedule = { schedule ->
                        viewModel.deleteSchedule(schedule)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Jadwal '${schedule.subjectName}' dihapus.")
                        }
                    },
                    onResetToDefault = {
                        viewModel.resetScheduleToDefault()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Jadwal Senin - Jumat berhasil diterapkan!")
                        }
                    }
                )

                2 -> SubjectListScreen(
                    allTasks = allTasks,
                    allSchedules = allSchedules,
                    onSelectSubjectForTasks = { subjectCode ->
                        viewModel.setSubjectFilter(subjectCode)
                        viewModel.setStatusFilter("SEMUA")
                        currentTab = 0
                    },
                    onAddNewTaskForSubject = { subject ->
                        taskToEdit = TaskItem(
                            title = "",
                            subjectCode = subject.code,
                            subjectName = subject.name,
                            dueDateTimeMillis = System.currentTimeMillis() + (24 * 3600 * 1000L)
                        )
                        showTaskDialog = true
                    }
                )
            }
        }
    }

    // Task Dialog (Add / Edit)
    if (showTaskDialog) {
        TaskDialog(
            taskToEdit = taskToEdit,
            onDismiss = { showTaskDialog = false },
            onSave = { task ->
                if (taskToEdit == null || taskToEdit?.id == 0L) {
                    viewModel.addTask(task)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tugas '${task.title}' berhasil ditambahkan dengan pengingat!")
                    }
                } else {
                    viewModel.updateTask(task)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tugas '${task.title}' berhasil diperbarui!")
                    }
                }
                showTaskDialog = false
            }
        )
    }

    // Schedule Dialog (Add / Edit)
    if (showScheduleDialog) {
        ScheduleDialog(
            scheduleToEdit = scheduleToEdit,
            defaultDay = selectedDay,
            onDismiss = { showScheduleDialog = false },
            onSave = { schedule ->
                if (scheduleToEdit == null) {
                    viewModel.addSchedule(schedule)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Jadwal '${schedule.subjectName}' berhasil ditambahkan!")
                    }
                } else {
                    viewModel.updateSchedule(schedule)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Jadwal '${schedule.subjectName}' berhasil diperbarui!")
                    }
                }
                showScheduleDialog = false
            }
        )
    }
}
