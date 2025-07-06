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
 * The main dashboard screen of the application.
 * It displays a list of created workouts and provides navigation to other features.
 *
 * @param workouts The list of workout plans to display.
 * @param onStartWorkout A callback invoked when the user taps on a workout to start it.
 * @param onNavigateToWorkoutCreation A callback to navigate to the workout creation screen.
 * @param onNavigateToExerciseLibrary A callback to navigate to the exercise library.
 * @param onNavigateToHistory A callback to navigate to the workout history screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<Workout>,
    onStartWorkout: (Workout) -> Unit,
    onNavigateToWorkoutCreation: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fitness Tracker") }
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

            // Display list of workouts or an empty state message.
            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't created any workouts yet.\nTap 'New Workout' to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workouts) { workout ->
                        WorkoutCard(workout = workout, onClick = { onStartWorkout(workout) })
                    }
                }
            }
        }
    }
}

/**
 * A card Composable to display a single workout plan in the list.
 *
 * @param workout The workout to display.
 * @param onClick The action to perform when the card is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCard(workout: Workout, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick, // Make the entire card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${workout.exerciseIds.size} exercises",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
