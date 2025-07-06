package com.example.progressiontracker
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * The screen that displays the user's workout history and visualizes volume progress.
 *
 * @param history A list of all completed workout records.
 * @param onNavigateBack A callback to navigate back to the home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    history: List<CompletedWorkout>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No workout history found.\nComplete a workout to see your progress!",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("Volume Over Time", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // The visualization chart
                VolumeChart(history = history, modifier = Modifier.fillMaxWidth().height(250.dp))

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Completed Workouts", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // The list of past workouts
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history.sortedByDescending { it.completionDate }) { workout ->
                        WorkoutHistoryItem(workout = workout)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryItem(workout: CompletedWorkout) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workout.workoutName, style = MaterialTheme.typography.titleMedium)
            Text(
                "Date: ${dateFormatter.format(workout.completionDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Duration: ${workout.durationInMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A custom chart to visualize workout volume over time.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun VolumeChart(history: List<CompletedWorkout>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val colors = listOf(Color.Blue, Color.Green, Color.Red, Color.Magenta, Color.Cyan, Color.Yellow, Color.Gray)

    // Process data for the chart
    val dataByGroup = remember(history) {
        history
            .flatMap { workout ->
                workout.volumeByOverallMuscleGroup.map { (group, volume) ->
                    Triple(workout.completionDate, group, volume)
                }
            }
            .groupBy { it.second } // Group by muscle group name
    }

    val maxVolume = remember(dataByGroup) {
        dataByGroup.values.flatten().maxOfOrNull { it.third }?.toFloat() ?: 1f
    }
    val datePoints = remember(history) {
        history.map { it.completionDate.time }.distinct().sorted()
    }
    val minDate = datePoints.firstOrNull() ?: 0L
    val maxDate = datePoints.lastOrNull() ?: (minDate + 1) // Avoid division by zero

    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }


    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val chartHeight = size.height
            val chartWidth = size.width
            val yAxisLabelWidth = 60f // Space for Y-axis labels
            val xAxisLabelHeight = 40f // Space for X-axis labels

            // Draw Y-axis labels and lines
            for (i in 0..4) {
                val y = chartHeight - xAxisLabelHeight - (i / 4f) * (chartHeight - xAxisLabelHeight)
                val volume = (maxVolume / 4f) * i
                drawLine(Color.LightGray, start = Offset(yAxisLabelWidth, y), end = Offset(chartWidth, y))
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${volume.toInt()} kg",
                    topLeft = Offset(0f, y - 8.sp.toPx()),
                    style = TextStyle(fontSize = 10.sp)
                )
            }

            // Draw X-axis labels and lines
            if (datePoints.size > 1) {
                datePoints.forEach { date ->
                    val x = yAxisLabelWidth + ((date - minDate).toFloat() / (maxDate - minDate)) * (chartWidth - yAxisLabelWidth)
                    drawLine(Color.LightGray, start = Offset(x, 0f), end = Offset(x, chartHeight - xAxisLabelHeight))
                    drawText(
                        textMeasurer = textMeasurer,
                        text = dateFormatter.format(Date(date)),
                        topLeft = Offset(x - 15.sp.toPx(), chartHeight - xAxisLabelHeight + 5.sp.toPx()),
                        style = TextStyle(fontSize = 10.sp)
                    )
                }
            }

            // Draw paths for each muscle group
            dataByGroup.entries.forEachIndexed { index, entry ->
                val path = Path()
                val points = entry.value.sortedBy { it.first.time }

                points.forEachIndexed { pointIndex, (date, _, volume) ->
                    val x = if (maxDate > minDate) {
                        yAxisLabelWidth + ((date.time - minDate).toFloat() / (maxDate - minDate)) * (chartWidth - yAxisLabelWidth)
                    } else {
                        yAxisLabelWidth + (chartWidth - yAxisLabelWidth) / 2
                    }
                    val y = chartHeight - xAxisLabelHeight - (volume.toFloat() / maxVolume) * (chartHeight - xAxisLabelHeight)

                    if (pointIndex == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(colors[index % colors.size], radius = 6f, center = Offset(x, y))
                }
                drawPath(path, color = colors[index % colors.size], style = Stroke(width = 4f))
            }
        }
        // Legend for the chart
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            dataByGroup.keys.forEachIndexed { index, groupName ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Box(modifier = Modifier.size(10.dp).padding(end = 4.dp).height(10.dp).width(10.dp).aspectRatio(1f, true).offset(y=1.dp)){
                        Canvas(modifier = Modifier.fillMaxSize()){
                            drawCircle(colors[index % colors.size])
                        }
                    }
                    Text(text = groupName, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
