package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
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
import com.example.data.SchoolSubjects
import com.example.data.Subject
import com.example.data.TaskItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    taskToEdit: TaskItem? = null,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember {
        Calendar.getInstance().apply {
            if (taskToEdit != null) {
                timeInMillis = taskToEdit.dueDateTimeMillis
            } else {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
        }
    }

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }

    var selectedSubject by remember {
        mutableStateOf(
            if (taskToEdit != null) SchoolSubjects.findByCode(taskToEdit.subjectCode)
            else SchoolSubjects.ALL_SUBJECTS.first()
        )
    }

    var reminderMinutes by remember {
        mutableIntStateOf(taskToEdit?.reminderMinutesBefore ?: 60)
    }

    var priority by remember {
        mutableStateOf(taskToEdit?.priority ?: "SEDANG")
    }

    var dueTimeMillis by remember {
        mutableLongStateOf(calendar.timeInMillis)
    }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var reminderMenuExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))

    val reminderOptions = listOf(
        0 to "Tepat pada waktu tenggat",
        30 to "30 Menit sebelumnya",
        60 to "1 Jam sebelumnya",
        120 to "2 Jam sebelumnya",
        1440 to "1 Hari (24 Jam) sebelumnya",
        -1 to "Matikan pengingat"
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
                    text = if (taskToEdit == null) "Tambah Tugas Baru" else "Edit Tugas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

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
                            .testTag("task_subject_dropdown")
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

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas *") },
                    placeholder = { Text("Contoh: Mengerjakan Tugas Modul 3") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan / Catatan (Opsional)") },
                    placeholder = { Text("Halaman buku, link referensi, catatan kelompok...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date and Time Row
                Text(
                    text = "Tenggat Waktu Pengerjaan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date picker button
                    OutlinedCard(
                        onClick = {
                            val tempCal = Calendar.getInstance().apply { timeInMillis = dueTimeMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    tempCal.set(Calendar.YEAR, year)
                                    tempCal.set(Calendar.MONTH, month)
                                    tempCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    dueTimeMillis = tempCal.timeInMillis
                                },
                                tempCal.get(Calendar.YEAR),
                                tempCal.get(Calendar.MONTH),
                                tempCal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("task_date_picker_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = dateFormat.format(Date(dueTimeMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }
                    }

                    // Time picker button
                    OutlinedCard(
                        onClick = {
                            val tempCal = Calendar.getInstance().apply { timeInMillis = dueTimeMillis }
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    tempCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    tempCal.set(Calendar.MINUTE, minute)
                                    dueTimeMillis = tempCal.timeInMillis
                                },
                                tempCal.get(Calendar.HOUR_OF_DAY),
                                tempCal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("task_time_picker_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = timeFormat.format(Date(dueTimeMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Reminder Dropdown
                ExposedDropdownMenuBox(
                    expanded = reminderMenuExpanded,
                    onExpandedChange = { reminderMenuExpanded = it }
                ) {
                    val currentReminderText = reminderOptions.find { it.first == reminderMinutes }?.second
                        ?: "1 Jam sebelumnya"

                    OutlinedTextField(
                        value = currentReminderText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Notifikasi Pengingat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = reminderMenuExpanded,
                        onDismissRequest = { reminderMenuExpanded = false }
                    ) {
                        reminderOptions.forEach { (mins, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    reminderMinutes = mins
                                    reminderMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Prioritas Tugas",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("RENDAH" to "Rendah", "SEDANG" to "Sedang", "TINGGI" to "Tinggi").forEach { (key, label) ->
                            FilterChip(
                                selected = priority == key,
                                onClick = { priority = key },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
                            if (title.isNotBlank()) {
                                val newTask = (taskToEdit ?: TaskItem(
                                    title = title.trim(),
                                    subjectCode = selectedSubject.code,
                                    subjectName = selectedSubject.name,
                                    dueDateTimeMillis = dueTimeMillis
                                )).copy(
                                    title = title.trim(),
                                    subjectCode = selectedSubject.code,
                                    subjectName = selectedSubject.name,
                                    description = description.trim(),
                                    dueDateTimeMillis = dueTimeMillis,
                                    reminderMinutesBefore = reminderMinutes,
                                    priority = priority
                                )
                                onSave(newTask)
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
