package com.example.progressiontracker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * The main screen for performing a workout.
 *
 * @param sessionDetails The details of the workout session, including the workout plan and the full exercise objects.
 * @param onFinishWorkout A callback invoked when the workout is finished. It passes the total duration in seconds
 * and a map of completed sets for summary calculation.
 * @param onNavigateBack A callback to navigate back, effectively canceling the workout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    sessionDetails: WorkoutSessionDetails,
    onFinishWorkout: (Long, Map<String, List<Boolean>>) -> Unit,
    onNavigateBack: () -> Unit
) {
    // State to track the checked status of each set for each exercise.
    // Key: Exercise ID, Value: List of booleans for each set's completion status.
    val completedSets = remember {
        mutableStateOf(
            sessionDetails.exercises.associate { exercise ->
                exercise.id to List(exercise.sets) { false }
            }
        )
    }

    // --- Timer States ---
    var totalTimeElapsed by remember { mutableStateOf(0L) }
    var restTimeRemaining by remember { mutableStateOf(0) }
    var isResting by remember { mutableStateOf(false) }

    // --- Timer Logic using LaunchedEffect ---

    // Total workout timer
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            totalTimeElapsed++
        }
    }

    // Rest timer
    LaunchedEffect(key1 = restTimeRemaining, key2 = isResting) {
        if (isResting && restTimeRemaining > 0) {
            delay(1000L)
            restTimeRemaining--
        } else {
            isResting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sessionDetails.workout.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("Exit") }
                },
                actions = {
                    Button(onClick = { onFinishWorkout(totalTimeElapsed, completedSets.value) }) {
                        Text("Finish")
                    }
                }
            )
        },
        bottomBar = {
            // The bottom bar shows the total time and the rest timer.
            WorkoutTimerBar(
                totalTimeElapsed = totalTimeElapsed,
                restTimeRemaining = restTimeRemaining,
                isResting = isResting
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sessionDetails.exercises) { exercise ->
                ExerciseInProgressCard(
                    exercise = exercise,
                    completedSetsForExercise = completedSets.value[exercise.id] ?: emptyList(),
                    onSetCompleted = { setIndex, isCompleted ->
                        val updatedSets = completedSets.value.toMutableMap()
                        val updatedSetStatus = updatedSets[exercise.id]?.toMutableList()
                        if (updatedSetStatus != null) {
                            updatedSetStatus[setIndex] = isCompleted
                            updatedSets[exercise.id] = updatedSetStatus
                            completedSets.value = updatedSets

                            // If a set was just completed, start the rest timer.
                            if (isCompleted) {
                                restTimeRemaining = sessionDetails.workout.restTimeInSeconds
                                isResting = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseInProgressCard(
    exercise: Exercise,
    completedSetsForExercise: List<Boolean>,
    onSetCompleted: (Int, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            // Create a row for each set with a checkbox.
            for (i in 0 until exercise.sets) {
                val repsForSet = exercise.reps.getOrElse(i) { 0 }
                SetRow(
                    setNumber = i + 1,
                    reps = repsForSet,
                    weight = exercise.weight,
                    isCompleted = completedSetsForExercise.getOrElse(i) { false },
                    onCheckedChange = { isChecked -> onSetCompleted(i, isChecked) }
                )
            }
        }
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    reps: Int,
    weight: Double,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Set $setNumber: $reps reps @ $weight kg",
            style = MaterialTheme.typography.bodyLarge
        )
        Checkbox(
            checked = isCompleted,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun WorkoutTimerBar(totalTimeElapsed: Long, restTimeRemaining: Int, isResting: Boolean) {
    val totalTimeFormatted = remember(totalTimeElapsed) {
        String.format(
            "%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(totalTimeElapsed),
            TimeUnit.SECONDS.toMinutes(totalTimeElapsed) % 60,
            totalTimeElapsed % 60
        )
    }

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Time", style = MaterialTheme.typography.labelSmall)
                Text(totalTimeFormatted, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            // The rest timer is only visible when isResting is true.
            AnimatedVisibility(
                visible = isResting,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("REST", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                    Text(
                        "$restTimeRemaining s",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}
