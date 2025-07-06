package com.example.progressiontracker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * A Jetpack Compose screen for creating a new exercise.
 * It provides input fields for all exercise properties and a save button.
 *
 * @param onSaveExercise A callback function that is invoked when the user clicks the save button.
 * It passes the newly created Exercise object.
 * @param onNavigateBack A callback function to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCreationScreen(
    onSaveExercise: (Exercise) -> Unit,
    onNavigateBack: () -> Unit
) {
    // State variables to hold the user's input for each field.
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var overallMuscleGroup by remember { mutableStateOf("") }
    var muscleGroups by remember { mutableStateOf("") }

    // State to track if there are any input errors.
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Exercise") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // In a real app, you'd use an Icon here e.g., Icons.Auto.Mirrored.Filled.ArrowBack
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // The main layout is a Column that scrolls if the content is too long for the screen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Text field for the exercise name.
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Text field for the overall muscle group.
            OutlinedTextField(
                value = overallMuscleGroup,
                onValueChange = { overallMuscleGroup = it },
                label = { Text("Overall Muscle Group (e.g., Push, Pull)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Text field for the specific muscle groups, comma-separated.
            OutlinedTextField(
                value = muscleGroups,
                onValueChange = { muscleGroups = it },
                label = { Text("Specific Muscles (e.g., Chest, Triceps)") },
                placeholder = { Text("Optional, comma-separated")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Row for numerical inputs: Sets, Reps, Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Text field for number of sets.
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Text field for weight.
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Text field for reps per set, comma-separated.
            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text("Reps per set (e.g., 12,10,8)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Display an error message if any fields are invalid.
            if (hasError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = {
                    // --- Input Validation and Parsing ---
                    val setsInt = sets.toIntOrNull()
                    val weightDouble = weight.toDoubleOrNull()
                    // This handles cases where the user might just put spaces or commas
                    val repsList = reps.split(",").mapNotNull { it.trim().toIntOrNull() }

                    // --- Validation Logic ---
                    when {
                        name.isBlank() -> {
                            hasError = true
                            errorMessage = "Exercise name cannot be empty."
                        }
                        overallMuscleGroup.isBlank() -> {
                            hasError = true
                            errorMessage = "Overall muscle group cannot be empty."
                        }
                        setsInt == null || setsInt <= 0 -> {
                            hasError = true
                            errorMessage = "Please enter a valid number of sets."
                        }
                        weightDouble == null || weightDouble < 0 -> {
                            hasError = true
                            errorMessage = "Please enter a valid weight."
                        }
                        repsList.isEmpty() -> {
                            hasError = true
                            errorMessage = "Please enter reps for the sets."
                        }
                        repsList.size != setsInt -> {
                            hasError = true
                            errorMessage = "The number of rep entries (${repsList.size}) must match the number of sets ($setsInt)."
                        }
                        else -> {
                            // --- Validation Success ---
                            hasError = false

                            // --- Create Exercise Object ---
                            val newExercise = Exercise(
                                name = name,
                                sets = setsInt,
                                reps = repsList,
                                weight = weightDouble,
                                overallMuscleGroup = overallMuscleGroup,
                                muscleGroups = muscleGroups.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )

                            // --- Trigger Callback ---
                            onSaveExercise(newExercise)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save Exercise")
            }
        }
    }
}
