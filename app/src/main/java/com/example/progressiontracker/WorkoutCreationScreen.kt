package com.example.progressiontracker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * A Jetpack Compose screen for creating a new workout plan.
 * Users can name the workout, set a rest time, and select exercises from a list.
 *
 * @param allExercises A list of all available exercises to choose from.
 * @param onSaveWorkout A callback function invoked when the user saves the workout.
 * @param onNavigateBack A callback function to navigate back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCreationScreen(
    allExercises: List<Exercise>,
    onSaveWorkout: (Workout) -> Unit,
    onNavigateBack: () -> Unit
) {
    // State for the workout name and rest time input fields.
    var workoutName by remember { mutableStateOf("") }
    var restTime by remember { mutableStateOf("") }

    // State to keep track of the IDs of the selected exercises. Using a set is efficient.
    val selectedExerciseIds = remember { mutableStateOf(setOf<String>()) }

    // State for input validation errors.
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Workout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // In a real app, use an Icon e.g., Icons.Auto.Mirrored.Filled.ArrowBack
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input field for the workout name.
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Workout Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Input field for the rest time.
            OutlinedTextField(
                value = restTime,
                onValueChange = { restTime = it },
                label = { Text("Rest Time (seconds)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Divider and title for the exercise list.
            HorizontalDivider()
            Text("Select Exercises:", style = MaterialTheme.typography.titleMedium)

            // Scrollable list of available exercises with checkboxes.
            LazyColumn(
                modifier = Modifier.weight(1f) // Takes up remaining space
            ) {
                items(allExercises) { exercise ->
                    ExerciseSelectionRow(
                        exercise = exercise,
                        isSelected = selectedExerciseIds.value.contains(exercise.id),
                        onToggleSelection = {
                            val currentSelection = selectedExerciseIds.value.toMutableSet()
                            if (currentSelection.contains(exercise.id)) {
                                currentSelection.remove(exercise.id)
                            } else {
                                currentSelection.add(exercise.id)
                            }
                            selectedExerciseIds.value = currentSelection
                        }
                    )
                }
            }

            // Display error message if validation fails.
            if (hasError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button.
            Button(
                onClick = {
                    val restTimeInt = restTime.toIntOrNull()
                    // --- Input Validation ---
                    when {
                        workoutName.isBlank() -> {
                            hasError = true
                            errorMessage = "Workout name cannot be empty."
                        }
                        restTimeInt == null || restTimeInt <= 0 -> {
                            hasError = true
                            errorMessage = "Please enter a valid rest time."
                        }
                        selectedExerciseIds.value.isEmpty() -> {
                            hasError = true
                            errorMessage = "Please select at least one exercise."
                        }
                        else -> {
                            // --- Validation Success ---
                            hasError = false
                            val newWorkout = Workout(
                                name = workoutName,
                                restTimeInSeconds = restTimeInt,
                                exerciseIds = selectedExerciseIds.value.toList()
                            )
                            onSaveWorkout(newWorkout)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Save Workout")
            }
        }
    }
}

/**
 * A Composable representing a single row in the exercise selection list.
 * It includes a checkbox and the exercise's name.
 *
 * @param exercise The exercise to display.
 * @param isSelected Whether the exercise is currently selected.
 * @param onToggleSelection Callback invoked when the row is clicked.
 */
@Composable
fun ExerciseSelectionRow(
    exercise: Exercise,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection) // Make the whole row clickable
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null // Checkbox state is controlled by the row's click
        )
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
