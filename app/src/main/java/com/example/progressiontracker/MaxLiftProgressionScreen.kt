package com.example.progressiontracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// --- Data class for this screen's chart ---
data class MaxLiftDataPoint(val date: Date, val maxWeight: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaxLiftProgressionScreen(
    allExercises: List<Exercise>,
    // In a real implementation, the ViewModel would provide the data points for the selected exercise
    liftDataPoints: List<MaxLiftDataPoint>,
    onExerciseSelected: (Exercise) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Max Lift Progression") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Text("Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dropdown menu to select an exercise
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedExercise?.name ?: "Select an Exercise",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exercise") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    allExercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text(exercise.name) },
                            onClick = {
                                selectedExercise = exercise
                                onExerciseSelected(exercise)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Chart Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(selectedExercise?.name ?: "Progression Chart", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    StockStyleChart(
                        dataPoints = liftDataPoints,
                        yAxisLabel = "Max Weight (kg)",
                        lineColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }
}

// A more generic version of our chart Composable
@OptIn(ExperimentalTextApi::class)
@Composable
private fun StockStyleChart(
    dataPoints: List<MaxLiftDataPoint>,
    yAxisLabel: String,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Not enough data to display chart.", textAlign = TextAlign.Center)
        }
        return
    }

    val maxValue = dataPoints.maxOfOrNull { it.maxWeight }?.toFloat() ?: 1f
    val minDate = dataPoints.minOfOrNull { it.date.time } ?: 0L
    val maxDate = dataPoints.maxOfOrNull { it.date.time } ?: (minDate + 1)

    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Canvas(modifier = modifier) {
        val chartHeight = size.height
        val chartWidth = size.width
        val yAxisLabelWidth = 60f
        val xAxisLabelHeight = 40f

        // Draw Y-axis labels and grid lines
        for (i in 0..4) {
            val y = chartHeight - xAxisLabelHeight - (i / 4f) * (chartHeight - xAxisLabelHeight)
            val value = (maxValue / 4f) * i
            drawLine(Color.LightGray, start = Offset(yAxisLabelWidth, y), end = Offset(chartWidth, y), strokeWidth = 1f)
            drawText(
                textMeasurer = textMeasurer,
                text = "${value.toInt()} kg",
                topLeft = Offset(0f, y - 8.sp.toPx()),
                style = TextStyle(fontSize = 10.sp)
            )
        }

        // Draw X-axis labels
        drawText(
            textMeasurer,
            dateFormatter.format(Date(minDate)),
            topLeft = Offset(yAxisLabelWidth, chartHeight - xAxisLabelHeight + 5.sp.toPx()),
            style = TextStyle(fontSize = 10.sp)
        )
        drawText(
            textMeasurer,
            dateFormatter.format(Date(maxDate)),
            topLeft = Offset(chartWidth - 30.sp.toPx(), chartHeight - xAxisLabelHeight + 5.sp.toPx()),
            style = TextStyle(fontSize = 10.sp)
        )

        // Draw the data path
        val path = Path()
        dataPoints.sortedBy { it.date.time }.forEachIndexed { index, point ->
            val x = if (maxDate > minDate) {
                yAxisLabelWidth + ((point.date.time - minDate).toFloat() / (maxDate - minDate)) * (chartWidth - yAxisLabelWidth)
            } else {
                yAxisLabelWidth + (chartWidth - yAxisLabelWidth) / 2
            }
            val y = chartHeight - xAxisLabelHeight - (point.maxWeight.toFloat() / maxValue) * (chartHeight - xAxisLabelHeight)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 5f))
    }
}
