package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.ScheduleItem
import com.example.data.SchoolSubjects
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    scheduleToEdit: ScheduleItem? = null,
    defaultDay: Int = 1,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit
) {
    val context = LocalContext.current

    var selectedDay by remember {
        mutableIntStateOf(scheduleToEdit?.dayOfWeek ?: defaultDay)
    }

    var selectedSubject by remember {
        mutableStateOf(
            if (scheduleToEdit != null) SchoolSubjects.findByCode(scheduleToEdit.subjectCode)
            else SchoolSubjects.ALL_SUBJECTS.first()
        )
    }

    var startTime by remember { mutableStateOf(scheduleToEdit?.startTime ?: "07:30") }
    var endTime by remember { mutableStateOf(scheduleToEdit?.endTime ?: "09:00") }
    var roomOrTeacher by remember { mutableStateOf(scheduleToEdit?.roomOrTeacher ?: "") }
    var reminderEnabled by remember { mutableStateOf(scheduleToEdit?.reminderEnabled ?: true) }

    var subjectMenuExpanded by remember { mutableStateOf(false) }

    val daysList = listOf(
        1 to "Senin",
        2 to "Selasa",
        3 to "Rabu",
        4 to "Kamis",
        5 to "Jumat",
        6 to "Sabtu",
        7 to "Minggu"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (scheduleToEdit == null) "Tambah Jadwal Pelajaran" else "Edit Jadwal Pelajaran",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Day Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Hari Pelajaran",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        daysList.forEach { (dayNum, dayName) ->
                            FilterChip(
                                selected = selectedDay == dayNum,
                                onClick = { selectedDay = dayNum },
                                label = { Text(dayName) }
                            )
                        }
                    }
                }

                // Subject Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedSubject.code} - ${selectedSubject.name}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mata Pelajaran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("schedule_subject_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        SchoolSubjects.ALL_SUBJECTS.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = subject.color,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.size(12.dp)
                                        ) {}
                                        Text("${subject.code} - ${subject.name}")
                                    }
                                },
                                onClick = {
                                    selectedSubject = subject
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Time Pickers Row
                Text(
                    text = "Waktu Jam Pelajaran",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start time picker
                    OutlinedCard(
                        onClick = {
                            val parts = startTime.split(":")
                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
                            val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    startTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                },
                                h, m, true
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("schedule_start_time_btn")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Jam Mulai", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(startTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // End time picker
                    OutlinedCard(
                        onClick = {
                            val parts = endTime.split(":")
                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    endTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                },
                                h, m, true
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("schedule_end_time_btn")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Jam Selesai", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(endTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Room / Teacher
                OutlinedTextField(
                    value = roomOrTeacher,
                    onValueChange = { roomOrTeacher = it },
                    label = { Text("Ruangan / Guru Pengampu (Opsional)") },
                    placeholder = { Text("Contoh: Lab Komputer 2 / Pak Budi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reminder switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pengingat Notifikasi",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Kirim notifikasi 15 menit sebelum kelas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val newSchedule = (scheduleToEdit ?: ScheduleItem(
                                dayOfWeek = selectedDay,
                                subjectCode = selectedSubject.code,
                                subjectName = selectedSubject.name,
                                startTime = startTime,
                                endTime = endTime
                            )).copy(
                                dayOfWeek = selectedDay,
                                subjectCode = selectedSubject.code,
                                subjectName = selectedSubject.name,
                                startTime = startTime,
                                endTime = endTime,
                                roomOrTeacher = roomOrTeacher.trim(),
                                reminderEnabled = reminderEnabled
                            )
                            onSave(newSchedule)
                        },
                        modifier = Modifier.testTag("save_schedule_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
