package com.example.progressiontracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A simple screen that shows a dialog to the user after they finish a workout.
 * This dialog asks if they want to update the original workout template with the
 * changes they made during the session.
 *
 * @param onSaveAndFinish Callback invoked with a boolean indicating the user's choice.
 * `true` to update the template, `false` to save only the session.
 * @param onCancel Callback to return to the active workout without finishing.
 */
@Composable
fun WorkoutSummaryScreen(
    onSaveAndFinish: (updateTemplate: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    // This screen's primary purpose is to host the confirmation dialog.
    // A more complex version could show a summary in the background.
    SaveChangesDialog(
        onDismiss = onCancel,
        onConfirm = { updateTemplate ->
            onSaveAndFinish(updateTemplate)
        }
    )
}

@Composable
private fun SaveChangesDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Workout") },
        text = {
            Text(
                "Do you want to update the original workout template with the reps and weight you performed in this session?",
                textAlign = TextAlign.Center
            )
        },
        // The confirm button is replaced by two distinct actions.
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onConfirm(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Yes, Update Template")
                }
                Button(
                    onClick = { onConfirm(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No, Just Save Session")
                }
            }
        },
        // The dismiss button acts as a "Cancel" to go back to the workout.
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
