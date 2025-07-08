package com.example.progressiontracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCreationScreenV4(
    allMuscles: List<Muscle>,
    existingExercise: Exercise?,
    onSaveExercise: (Exercise) -> Unit,
    onUpsertMuscle: (Muscle) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isEditing = existingExercise != null
    val screenTitle = if (isEditing) "Edit Exercise" else "Create New Exercise"

    var name by remember { mutableStateOf(existingExercise?.name ?: "") }
    val primaryMuscles = remember { mutableStateListOf<Muscle>() }
    val secondaryMuscles = remember { mutableStateListOf<Muscle>() }
    val tertiaryMuscles = remember { mutableStateListOf<Muscle>() }

    var showPickerFor by remember { mutableStateOf<MuscleTier?>(null) }

    LaunchedEffect(existingExercise, allMuscles) {
        if (isEditing && allMuscles.isNotEmpty()) {
            primaryMuscles.clear()
            secondaryMuscles.clear()
            tertiaryMuscles.clear()
            // THE FIX: Added safe calls (?.) to handle the nullable existingExercise
            primaryMuscles.addAll(allMuscles.filter { it.id in (existingExercise?.primaryMuscleIds ?: emptyList()) })
            secondaryMuscles.addAll(allMuscles.filter { it.id in (existingExercise?.secondaryMuscleIds ?: emptyList()) })
            tertiaryMuscles.addAll(allMuscles.filter { it.id in (existingExercise?.tertiaryMuscleIds ?: emptyList()) })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    MuscleTierEditor("Primary Muscles", primaryMuscles, onAddClick = { showPickerFor = MuscleTier.PRIMARY }) { muscle ->
                        primaryMuscles.remove(muscle)
                    }
                }
                item {
                    MuscleTierEditor("Secondary Muscles", secondaryMuscles, onAddClick = { showPickerFor = MuscleTier.SECONDARY }) { muscle ->
                        secondaryMuscles.remove(muscle)
                    }
                }
                item {
                    MuscleTierEditor("Tertiary Muscles", tertiaryMuscles, onAddClick = { showPickerFor = MuscleTier.TERTIARY }) { muscle ->
                        tertiaryMuscles.remove(muscle)
                    }
                }
            }

            Button(
                onClick = {
                    val finalExercise = (existingExercise ?: Exercise(name = "", primaryMuscleIds = listOf(), secondaryMuscleIds = listOf(), tertiaryMuscleIds = listOf()))
                        .copy(
                            name = name,
                            primaryMuscleIds = primaryMuscles.map { it.id },
                            secondaryMuscleIds = secondaryMuscles.map { it.id },
                            tertiaryMuscleIds = tertiaryMuscles.map { it.id }
                        )
                    onSaveExercise(finalExercise)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = name.isNotBlank() && primaryMuscles.isNotEmpty()
            ) {
                Text("Save Exercise")
            }
        }
    }

    if (showPickerFor != null) {
        MusclePickerDialog(
            allMuscles = allMuscles,
            onDismiss = { showPickerFor = null },
            onMuscleSelected = { muscle ->
                when (showPickerFor) {
                    MuscleTier.PRIMARY -> if (!primaryMuscles.contains(muscle)) primaryMuscles.add(muscle)
                    MuscleTier.SECONDARY -> if (!secondaryMuscles.contains(muscle)) secondaryMuscles.add(muscle)
                    MuscleTier.TERTIARY -> if (!tertiaryMuscles.contains(muscle)) tertiaryMuscles.add(muscle)
                    null -> {}
                }
                showPickerFor = null
            },
            onUpsertMuscle = onUpsertMuscle
        )
    }
}

@Composable
private fun MuscleTierEditor(
    title: String,
    selectedMuscles: List<Muscle>,
    onAddClick: () -> Unit,
    onRemoveClick: (Muscle) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Muscle")
                }
            }
            Spacer(Modifier.height(8.dp))
            selectedMuscles.forEach { muscle ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(muscle.name)
                    IconButton(onClick = { onRemoveClick(muscle) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Muscle")
                    }
                }
            }
        }
    }
}

@Composable
private fun MusclePickerDialog(
    allMuscles: List<Muscle>,
    onDismiss: () -> Unit,
    onMuscleSelected: (Muscle) -> Unit,
    onUpsertMuscle: (Muscle) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredMuscles = allMuscles.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Muscle", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredMuscles) { muscle ->
                        Text(
                            text = muscle.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMuscleSelected(muscle) }
                                .padding(vertical = 16.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showCreateDialog = true }) { Text("Create New") }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateMuscleDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { muscle ->
                onUpsertMuscle(muscle)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateMuscleDialog(
    onDismiss: () -> Unit,
    onConfirm: (Muscle) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Muscle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Muscle Name") })
                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Overall Group (e.g., Chest)") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(Muscle(name = name, overallMuscleGroup = group, isUserAdded = true)) },
                enabled = name.isNotBlank() && group.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private enum class MuscleTier { PRIMARY, SECONDARY, TERTIARY }
