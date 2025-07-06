package com.example.progressiontracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    activeExercises: List<ActiveExercise>,
    onFinishWorkout: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var totalTimeElapsed by remember { mutableStateOf(0L) }
    var restTimeRemaining by remember { mutableStateOf(0) }
    var isResting by remember { mutableStateOf(false) }

    // Total workout timer
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            if (!isResting) { // Only increment total time when not resting
                totalTimeElapsed++
            }
        }
    }

    // Rest timer
    LaunchedEffect(key1 = restTimeRemaining, key2 = isResting) {
        if (isResting && restTimeRemaining > 0) {
            delay(1000L)
            restTimeRemaining--
        } else if (isResting && restTimeRemaining <= 0) {
            isResting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Workout") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Text("Cancel") } },
                actions = {
                    Button(onClick = { onFinishWorkout(totalTimeElapsed) }) {
                        Text("Finish")
                    }
                }
            )
        },
        bottomBar = {
            WorkoutTimerBar(
                totalTimeElapsed = totalTimeElapsed,
                restTimeRemaining = restTimeRemaining,
                isResting = isResting,
                onSkipRest = {
                    isResting = false
                    restTimeRemaining = 0
                }
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
            items(activeExercises) { activeExercise ->
                ActiveExerciseCard(
                    activeExercise = activeExercise,
                    onSetCompleted = { set ->
                        set.isCompleted = !set.isCompleted
                        if (set.isCompleted) {
                            restTimeRemaining = set.rest.toIntOrNull() ?: 0
                            isResting = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveExerciseCard(
    activeExercise: ActiveExercise,
    onSetCompleted: (ActiveWorkoutSet) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(activeExercise.exercise.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            activeExercise.sets.forEach { set ->
                ActiveSetRow(
                    activeSet = set,
                    onCheckedChange = { onSetCompleted(set) }
                )
            }
        }
    }
}

@Composable
private fun ActiveSetRow(
    activeSet: ActiveWorkoutSet,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = activeSet.isCompleted,
            onCheckedChange = { onCheckedChange() }
        )
        Text("Set ${activeSet.setNumber}")
        OutlinedTextField(
            value = activeSet.reps,
            onValueChange = { activeSet.reps = it },
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !activeSet.isCompleted
        )
        OutlinedTextField(
            value = activeSet.weight,
            onValueChange = { activeSet.weight = it },
            label = { Text("kg") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = !activeSet.isCompleted
        )
    }
}

@Composable
private fun WorkoutTimerBar(
    totalTimeElapsed: Long,
    restTimeRemaining: Int,
    isResting: Boolean,
    onSkipRest: () -> Unit
) {
    val totalTimeFormatted = remember(totalTimeElapsed) {
        String.format(
            "%02d:%02d",
            TimeUnit.SECONDS.toMinutes(totalTimeElapsed),
            totalTimeElapsed % 60
        )
    }

    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Workout Time", style = MaterialTheme.typography.labelMedium)
                Text(totalTimeFormatted, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            AnimatedVisibility(visible = isResting) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("REST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "$restTimeRemaining s",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(onClick = onSkipRest) {
                        Text("Skip")
                    }
                }
            }
        }
    }
}
