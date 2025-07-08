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
 * The main dashboard screen for the v4.0 application.
 * Includes navigation to the new progression screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<WorkoutWithSets>,
    onStartWorkout: (WorkoutWithSets) -> Unit,
    onNavigateToWorkoutCreation: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onNavigateToVolumeProgression: () -> Unit,
    onNavigateToMaxLiftProgression: () -> Unit
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
            // --- Main Navigation Buttons ---
            Button(onClick = onNavigateToWorkoutCreation, modifier = Modifier.fillMaxWidth()) {
                Text("Create New Workout")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onNavigateToVolumeProgression, modifier = Modifier.weight(1f)) {
                    Text("Volume Stats")
                }
                Button(onClick = onNavigateToMaxLiftProgression, modifier = Modifier.weight(1f)) {
                    Text("Max Lift Stats")
                }
            }
            Button(onClick = onNavigateToExerciseLibrary, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Exercises")
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("My Workouts", style = MaterialTheme.typography.titleLarge)

            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't created any workouts yet.\nTap 'Create New Workout' to get started.",
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
