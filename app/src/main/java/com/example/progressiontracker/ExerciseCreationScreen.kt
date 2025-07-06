package com.example.progressiontracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A screen for both creating a new exercise and editing an existing one.
 *
 * @param existingExercise The exercise to be edited. If null, the screen is in "create" mode.
 * @param onSaveExercise A callback to save the exercise (new or updated).
 * @param onNavigateBack A callback to navigate back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCreationScreen(
    existingExercise: Exercise?,
    onSaveExercise: (Exercise) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isEditing = existingExercise != null
    val screenTitle = if (isEditing) "Edit Exercise" else "Create New Exercise"

    var name by remember { mutableStateOf(existingExercise?.name ?: "") }
    var overallMuscleGroup by remember { mutableStateOf(existingExercise?.overallMuscleGroup ?: "") }
    var muscleGroups by remember { mutableStateOf(existingExercise?.muscleGroups?.joinToString(", ") ?: "") }
    var hasError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = overallMuscleGroup,
                onValueChange = { overallMuscleGroup = it },
                label = { Text("Overall Muscle Group (e.g., Push, Pull)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = muscleGroups,
                onValueChange = { muscleGroups = it },
                label = { Text("Specific Muscles (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (hasError) {
                Text(
                    text = "Name and Overall Muscle Group cannot be empty.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && overallMuscleGroup.isNotBlank()) {
                        hasError = false
                        val finalExercise = (existingExercise ?: Exercise(name = "", overallMuscleGroup = "", muscleGroups = listOf()))
                            .copy(
                                name = name,
                                overallMuscleGroup = overallMuscleGroup,
                                muscleGroups = muscleGroups.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                        onSaveExercise(finalExercise)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save Exercise")
            }
        }
    }
}
