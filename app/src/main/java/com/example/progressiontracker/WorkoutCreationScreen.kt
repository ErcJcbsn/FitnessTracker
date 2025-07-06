package com.example.progressiontracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.UUID

/**
 * This class now holds its own state using mutableStateOf.
 * This makes each property individually observable by Compose.
 * It's no longer a data class.
 */
private class EditableSet(
    val id: String = UUID.randomUUID().toString(),
    initialReps: String,
    initialWeight: String,
    initialRest: String
) {
    var reps by mutableStateOf(initialReps)
    var weight by mutableStateOf(initialWeight)
    var rest by mutableStateOf(initialRest)
}

private data class EditableExercise(
    val id: String = UUID.randomUUID().toString(),
    val exercise: Exercise,
    val sets: MutableList<EditableSet>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCreationScreen(
    allExercises: List<Exercise>,
    onSaveWorkout: (Workout, List<WorkoutSet>) -> Unit,
    onNavigateBack: () -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    val editableExercises = remember { mutableStateListOf<EditableExercise>() }
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Workout Template") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Text("Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Workout Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(editableExercises, key = { it.id }) { editableExercise ->
                    ExerciseTemplateCard(
                        editableExercise = editableExercise,
                        onAddSet = {
                            // THE FIX: Check for the last set and copy its values.
                            val lastSet = editableExercise.sets.lastOrNull()
                            val newSet = if (lastSet != null) {
                                // If a previous set exists, copy its values.
                                EditableSet(
                                    initialReps = lastSet.reps,
                                    initialWeight = lastSet.weight,
                                    initialRest = lastSet.rest
                                )
                            } else {
                                // Otherwise, use default values for the very first set.
                                EditableSet(initialReps = "10", initialWeight = "50", initialRest = "60")
                            }
                            editableExercise.sets.add(newSet)
                        },
                        onDeleteSet = { set ->
                            editableExercise.sets.remove(set)
                        },
                        onDeleteExercise = {
                            editableExercises.remove(editableExercise)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Exercise")
                }
                Button(
                    onClick = {
                        val workout = Workout(name = workoutName)
                        val workoutSets = mutableListOf<WorkoutSet>()
                        editableExercises.forEach { editableEx ->
                            editableEx.sets.forEachIndexed { index, editableSet ->
                                workoutSets.add(
                                    WorkoutSet(
                                        workoutId = workout.id,
                                        exerciseId = editableEx.exercise.id,
                                        setNumber = index + 1,
                                        targetReps = editableSet.reps.toIntOrNull() ?: 0,
                                        targetWeight = editableSet.weight.toDoubleOrNull() ?: 0.0,
                                        targetRestInSeconds = editableSet.rest.toIntOrNull() ?: 0
                                    )
                                )
                            }
                        }
                        onSaveWorkout(workout, workoutSets)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = workoutName.isNotBlank() && editableExercises.isNotEmpty()
                ) {
                    Text("Save Workout")
                }
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = allExercises,
            onDismiss = { showExercisePicker = false },
            onExerciseSelected = { exercise ->
                editableExercises.add(
                    EditableExercise(
                        exercise = exercise,
                        sets = mutableStateListOf(
                            EditableSet(initialReps = "10", initialWeight = "50", initialRest = "60")
                        )
                    )
                )
                showExercisePicker = false
            }
        )
    }
}

@Composable
private fun ExerciseTemplateCard(
    editableExercise: EditableExercise,
    onAddSet: () -> Unit,
    onDeleteSet: (EditableSet) -> Unit,
    onDeleteExercise: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(editableExercise.exercise.name, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDeleteExercise) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                }
            }
            Spacer(Modifier.height(8.dp))
            editableExercise.sets.forEachIndexed { index, set ->
                key(set.id) {
                    SetEditorRow(
                        setNumber = index + 1,
                        editableSet = set,
                        onDelete = { onDeleteSet(set) }
                    )
                }
            }
            Button(onClick = onAddSet, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                Text("Add Set")
            }
        }
    }
}

@Composable
private fun SetEditorRow(
    setNumber: Int,
    editableSet: EditableSet,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Set $setNumber")
        OutlinedTextField(
            value = editableSet.reps,
            onValueChange = { editableSet.reps = it },
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = editableSet.weight,
            onValueChange = { editableSet.weight = it },
            label = { Text("kg") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = editableSet.rest,
            onValueChange = { editableSet.rest = it },
            label = { Text("Rest(s)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Set")
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select an Exercise") },
        text = {
            LazyColumn {
                items(exercises, key = { it.id }) { exercise ->
                    Text(
                        text = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise) }
                            .padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
