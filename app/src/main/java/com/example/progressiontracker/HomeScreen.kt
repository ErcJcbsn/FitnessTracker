package com.example.progressiontracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The main dashboard screen of the v2.0 application.
 *
 * @param workouts The list of workout templates (with their sets) to display.
 * @param onStartWorkout Callback invoked when the user taps on a workout to start it.
 * @param onNavigateToWorkoutCreation Callback to navigate to the workout creation screen.
 * @param onNavigateToExerciseLibrary Callback to navigate to the exercise library.
 * @param onNavigateToHistory Callback to navigate to the workout history screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<WorkoutWithSets>,
    onStartWorkout: (WorkoutWithSets) -> Unit,
    onNavigateToWorkoutCreation: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progression Tracker") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onNavigateToWorkoutCreation, modifier = Modifier.weight(1f)) {
                    Text("New Workout")
                }
                Button(onClick = onNavigateToExerciseLibrary, modifier = Modifier.weight(1f)) {
                    Text("Exercises")
                }
            }
            Button(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) {
                Text("View Workout History")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("My Workouts", style = MaterialTheme.typography.titleLarge)

            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't created any workouts yet.\nTap 'New Workout' to get started.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workouts) { workoutWithSets ->
                        WorkoutCard(
                            workoutWithSets = workoutWithSets,
                            onClick = { onStartWorkout(workoutWithSets) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCard(workoutWithSets: WorkoutWithSets, onClick: () -> Unit) {
    // Calculate the number of unique exercises in the workout
    val exerciseCount = workoutWithSets.sets.map { it.exerciseId }.distinct().size

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = workoutWithSets.workout.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$exerciseCount exercises, ${workoutWithSets.sets.size} total sets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
