package com.example.progressiontracker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Jetpack Compose screen that displays a list of all available exercises.
 * It also provides a button to navigate to the exercise creation screen.
 *
 * @param exercises The list of Exercise objects to be displayed.
 * @param onAddExercise A callback function to be invoked when the user wants to add a new exercise.
 * @param onNavigateBack A callback function to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    exercises: List<Exercise>,
    onAddExercise: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Library") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // In a real app, you'd use an Icon here e.g., Icons.Auto.Mirrored.Filled.ArrowBack
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExercise) {
                // In a real app, you'd use an Icon here e.g., Icons.Default.Add
                Text("+", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        // Check if the list of exercises is empty.
        if (exercises.isEmpty()) {
            // If it's empty, display a message in the center of the screen.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your exercise library is empty.\nTap the '+' button to add a new exercise.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // If the list is not empty, display the exercises in a LazyColumn.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseListItem(exercise = exercise)
                }
            }
        }
    }
}

/**
 * A Composable that represents a single item in the exercise list.
 * It displays the key details of an exercise in a styled card.
 *
 * @param exercise The Exercise object to display.
 */
@Composable
fun ExerciseListItem(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display Exercise Name
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Display Overall Muscle Group
            Text(
                text = "Category: ${exercise.overallMuscleGroup}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Display Sets, Reps, and Weight details
            Text(
                text = "${exercise.sets} sets of ${exercise.reps.joinToString("/")} reps @ ${exercise.weight} kg",
                style = MaterialTheme.typography.bodyMedium
            )

            // Display specific muscle groups if available
            if (exercise.muscleGroups.isNotEmpty()) {
                Text(
                    text = "Muscles: ${exercise.muscleGroups.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
