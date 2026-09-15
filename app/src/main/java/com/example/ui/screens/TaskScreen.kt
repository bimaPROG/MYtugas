package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.ui.components.SubjectFilterChips
import com.example.ui.components.TaskCard
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusUrgent
import com.example.ui.viewmodel.TaskStats

@Composable
fun TaskScreen(
    tasks: List<TaskItem>,
    stats: TaskStats,
    selectedSubject: String?,
    selectedStatus: String,
    onSelectSubject: (String?) -> Unit,
    onSelectStatus: (String) -> Unit,
    onToggleCompleted: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("task_list_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Summary Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Aktif
                StatCard(
                    title = "Tugas Aktif",
                    count = stats.active,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Mendekati Tenggat
                StatCard(
                    title = "Mendekati",
                    count = stats.urgent,
                    color = StatusPending,
                    modifier = Modifier.weight(1f)
                )

                // Selesai
                StatCard(
                    title = "Selesai",
                    count = stats.completed,
                    color = StatusDone,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Status Segmented Filter (Semua, Aktif, Selesai)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("SEMUA" to "Semua", "AKTIF" to "Belum Selesai", "SELESAI" to "Selesai").forEach { (statusKey, label) ->
                    FilterChip(
                        selected = selectedStatus == statusKey,
                        onClick = { onSelectStatus(statusKey) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Subject Chips Filter
        item {
            SubjectFilterChips(
                selectedSubject = selectedSubject,
                onSelectSubject = onSelectSubject
            )
        }

        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (selectedSubject != null) "Tidak ada tugas untuk mata pelajaran $selectedSubject"
                            else if (selectedStatus == "SELESAI") "Belum ada tugas yang selesai"
                            else "Belum ada tugas yang tercatat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tekan tombol + untuk menambahkan tugas baru",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleCompleted = { onToggleCompleted(task) },
                    onEditClick = { onEditTask(task) },
                    onDeleteClick = { onDeleteTask(task) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
