package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SchoolSubjects

@Composable
fun SubjectFilterChips(
    selectedSubject: String?,
    onSelectSubject: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedSubject == null,
            onClick = { onSelectSubject(null) },
            label = { Text("Semua Mapel") },
            modifier = Modifier.testTag("filter_all_subjects")
        )

        SchoolSubjects.ALL_SUBJECTS.forEach { subject ->
            FilterChip(
                selected = selectedSubject.equals(subject.code, ignoreCase = true),
                onClick = {
                    if (selectedSubject.equals(subject.code, ignoreCase = true)) {
                        onSelectSubject(null)
                    } else {
                        onSelectSubject(subject.code)
                    }
                },
                label = { Text(subject.code) },
                modifier = Modifier.testTag("filter_subject_${subject.code}")
            )
        }
    }
}
