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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// --- Data classes for chart processing ---
data class ChartDataPoint(val date: Date, val volume: Double)
data class ChartDataSet(val muscleOrGroupName: String, val points: List<ChartDataPoint>)

enum class TimeFrame { Daily, Monthly, Yearly, AllTime }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeProgressionScreen(
    // In a real implementation, this data would be processed in the ViewModel
    chartDataSets: List<ChartDataSet>,
    onNavigateBack: () -> Unit
) {
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.AllTime) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volume Progression") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Text("Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TimeFrameSelector(
                selectedTimeFrame = selectedTimeFrame,
                onTimeFrameSelected = { selectedTimeFrame = it }
            )
            Spacer(Modifier.height(16.dp))

            if (chartDataSets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No workout history found to display progression.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(chartDataSets) { dataSet ->
                        VolumeChartCard(dataSet = dataSet)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFrameSelector(
    selectedTimeFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TimeFrame.values().forEachIndexed { index, timeFrame ->
            SegmentedButton(
                // THE FIX: Changed 'position' to 'index' to match the correct API
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeFrame.values().size),
                onClick = { onTimeFrameSelected(timeFrame) },
                selected = timeFrame == selectedTimeFrame
            ) {
                Text(timeFrame.name)
            }
        }
    }
}

@Composable
private fun VolumeChartCard(dataSet: ChartDataSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dataSet.muscleOrGroupName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            // Placeholder for the trend indicator bar from the mockup
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).padding(vertical = 4.dp)) {
                // TODO: Implement trend indicator logic
            }
            Spacer(Modifier.height(8.dp))
            StockStyleChart(
                dataPoints = dataSet.points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun StockStyleChart(dataPoints: List<ChartDataPoint>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Not enough data to display chart.")
        }
        return
    }

    val maxVolume = dataPoints.maxOfOrNull { it.volume }?.toFloat() ?: 1f
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
            val volume = (maxVolume / 4f) * i
            drawLine(Color.LightGray, start = Offset(yAxisLabelWidth, y), end = Offset(chartWidth, y), strokeWidth = 1f)
            drawText(
                textMeasurer = textMeasurer,
                text = "${volume.toInt()} kg",
                topLeft = Offset(0f, y - 8.sp.toPx()),
                style = TextStyle(fontSize = 10.sp)
            )
        }

        // Draw X-axis labels (simplified)
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

        // Draw the volume path
        val path = Path()
        dataPoints.sortedBy { it.date.time }.forEachIndexed { index, point ->
            val x = if (maxDate > minDate) {
                yAxisLabelWidth + ((point.date.time - minDate).toFloat() / (maxDate - minDate)) * (chartWidth - yAxisLabelWidth)
            } else {
                yAxisLabelWidth + (chartWidth - yAxisLabelWidth) / 2
            }
            val y = chartHeight - xAxisLabelHeight - (point.volume.toFloat() / maxVolume) * (chartHeight - xAxisLabelHeight)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Color.Red, style = Stroke(width = 5f))
    }
}
