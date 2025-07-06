package com.example.progressiontracker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A screen that displays a summary of a completed workout, including duration and volume per muscle group.
 *
 * @param sessionDetails The details of the workout that was just completed.
 * @param totalDurationInSeconds The total time spent on the workout.
 * @param completedSets A map indicating which sets were completed for each exercise.
 * @param onSaveAndFinish A callback invoked when the user clicks the final button. It passes a
 * `CompletedWorkout` object ready to be saved to the database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    sessionDetails: WorkoutSessionDetails,
    totalDurationInSeconds: Long,
    completedSets: Map<String, List<Boolean>>,
    onSaveAndFinish: (CompletedWorkout) -> Unit
) {
    // Calculate the volumes once when the screen is first composed.
    val summary = remember(sessionDetails, completedSets) {
        calculateWorkoutSummary(sessionDetails, completedSets)
    }

    val durationInMinutes = TimeUnit.SECONDS.toMinutes(totalDurationInSeconds).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Summary") }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Workout Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Display Total Duration
                SummaryStat(
                    label = "Total Duration",
                    value = "$durationInMinutes minutes"
                )

                HorizontalDivider()

                // Display Volume per Muscle Group
                Text(
                    "Volume Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyColumn {
                    items(summary.volumeByOverallMuscleGroup.entries.toList()) { (group, volume) ->
                        VolumeRow(label = group, volume = volume)
                    }
                }
            }

            Button(
                onClick = {
                    // Create the final object to be saved to history.
                    val completedWorkout = CompletedWorkout(
                        workoutName = sessionDetails.workout.name,
                        completionDate = Date(), // Current date and time
                        durationInMinutes = durationInMinutes,
                        volumeByMuscleGroup = summary.volumeBySpecificMuscleGroup,
                        volumeByOverallMuscleGroup = summary.volumeByOverallMuscleGroup
                    )
                    onSaveAndFinish(completedWorkout)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Save & Finish")
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VolumeRow(label: String, volume: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${"%.1f".format(volume)} kg",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * A helper data class to hold the results of the summary calculation.
 */
private data class WorkoutSummary(
    val volumeBySpecificMuscleGroup: Map<String, Double>,
    val volumeByOverallMuscleGroup: Map<String, Double>
)

/**
 * A helper function to calculate the total volume for each muscle group based on completed sets.
 */
private fun calculateWorkoutSummary(
    sessionDetails: WorkoutSessionDetails,
    completedSets: Map<String, List<Boolean>>
): WorkoutSummary {
    val volumeBySpecific = mutableMapOf<String, Double>().withDefault { 0.0 }
    val volumeByOverall = mutableMapOf<String, Double>().withDefault { 0.0 }

    // Iterate over each exercise in the workout
    for (exercise in sessionDetails.exercises) {
        val setsStatus = completedSets[exercise.id] ?: continue
        var exerciseOverallVolume = 0.0

        // Iterate over each set for the current exercise
        setsStatus.forEachIndexed { setIndex, isCompleted ->
            if (isCompleted) {
                val reps = exercise.reps.getOrElse(setIndex) { 0 }
                val volumeForSet = reps * exercise.weight

                exerciseOverallVolume += volumeForSet

                // Add volume to each specific muscle group for that exercise
                exercise.muscleGroups.forEach { group ->
                    if(group.isNotBlank()) {
                        volumeBySpecific[group] = volumeBySpecific.getValue(group) + volumeForSet
                    }
                }
            }
        }
        // Add the total volume from this exercise to its overall category
        if(exercise.overallMuscleGroup.isNotBlank()){
            volumeByOverall[exercise.overallMuscleGroup] =
                volumeByOverall.getValue(exercise.overallMuscleGroup) + exerciseOverallVolume
        }
    }
    return WorkoutSummary(volumeBySpecific, volumeByOverall)
}
