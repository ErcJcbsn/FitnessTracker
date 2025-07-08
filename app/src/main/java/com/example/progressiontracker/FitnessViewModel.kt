package com.example.progressiontracker

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    // --- StateFlows for observing lists from the database ---
    val allMuscles: StateFlow<List<Muscle>> = repository.allMuscles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutsWithSets: StateFlow<List<WorkoutWithSets>> = repository.allWorkoutsWithSets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State for Progression Screens ---
    private val _volumeProgressionData = MutableStateFlow<List<ChartDataSet>>(emptyList())
    val volumeProgressionData: StateFlow<List<ChartDataSet>> = _volumeProgressionData

    private val _maxLiftProgressionData = MutableStateFlow<List<MaxLiftDataPoint>>(emptyList())
    val maxLiftProgressionData: StateFlow<List<MaxLiftDataPoint>> = _maxLiftProgressionData

    init {
        // When the ViewModel is created, start processing the history for the volume chart
        viewModelScope.launch {
            repository.allCompletedWorkoutsWithSets.collect { history ->
                processVolumeHistory(history)
            }
        }
    }

    private fun processVolumeHistory(history: List<CompletedWorkoutWithSets>) {
        // This is where the volume distribution logic happens
        val volumePerMusclePerDay = mutableMapOf<Pair<Date, String>, Double>()

        history.forEach { completedWorkoutWithSets ->
            val workoutDate = completedWorkoutWithSets.completedWorkout.completionDate
            completedWorkoutWithSets.sets.forEach { completedSet ->
                val exercise = allExercises.value.find { it.id == completedSet.exerciseId } ?: return@forEach
                val totalSetVolume = completedSet.actualReps * completedSet.actualWeight

                // --- Volume Distribution Logic ---
                val primaryWeight = 60; val secondaryWeight = 30; val tertiaryWeight = 10
                var totalPoints = 0
                if (exercise.primaryMuscleIds.isNotEmpty()) totalPoints += primaryWeight
                if (exercise.secondaryMuscleIds.isNotEmpty()) totalPoints += secondaryWeight
                if (exercise.tertiaryMuscleIds.isNotEmpty()) totalPoints += tertiaryWeight

                if (totalPoints == 0) return@forEach

                // Distribute to primary muscles
                if (exercise.primaryMuscleIds.isNotEmpty()) {
                    val volumePerPrimaryMuscle = (totalSetVolume * (primaryWeight.toDouble() / totalPoints)) / exercise.primaryMuscleIds.size
                    exercise.primaryMuscleIds.forEach { muscleId ->
                        val key = workoutDate to muscleId
                        volumePerMusclePerDay[key] = (volumePerMusclePerDay[key] ?: 0.0) + volumePerPrimaryMuscle
                    }
                }

                // Distribute to secondary muscles
                if (exercise.secondaryMuscleIds.isNotEmpty()) {
                    val volumePerSecondaryMuscle = (totalSetVolume * (secondaryWeight.toDouble() / totalPoints)) / exercise.secondaryMuscleIds.size
                    exercise.secondaryMuscleIds.forEach { muscleId ->
                        val key = workoutDate to muscleId
                        volumePerMusclePerDay[key] = (volumePerMusclePerDay[key] ?: 0.0) + volumePerSecondaryMuscle
                    }
                }

                // Distribute to tertiary muscles
                if (exercise.tertiaryMuscleIds.isNotEmpty()) {
                    val volumePerTertiaryMuscle = (totalSetVolume * (tertiaryWeight.toDouble() / totalPoints)) / exercise.tertiaryMuscleIds.size
                    exercise.tertiaryMuscleIds.forEach { muscleId ->
                        val key = workoutDate to muscleId
                        volumePerMusclePerDay[key] = (volumePerMusclePerDay[key] ?: 0.0) + volumePerTertiaryMuscle
                    }
                }
            }
        }

        // Group by muscle and create ChartDataSets
        val finalChartData = volumePerMusclePerDay.entries
            .groupBy { it.key.second } // Group by muscleId
            .map { (muscleId, entries) ->
                val muscleName = allMuscles.value.find { it.id == muscleId }?.name ?: "Unknown"
                val points = entries.map { ChartDataPoint(it.key.first, it.value) }
                ChartDataSet(muscleName, points)
            }
        _volumeProgressionData.value = finalChartData
    }

    fun processMaxLiftHistory(exerciseId: String) = viewModelScope.launch {
        repository.allCompletedWorkoutsWithSets.firstOrNull()?.let { history ->
            val liftData = history
                .flatMap { it.sets }
                .filter { it.exerciseId == exerciseId }
                .groupBy { it.completedWorkoutId } // Group by workout session
                .mapNotNull { (_, sets) ->
                    val maxWeightInSession = sets.maxOfOrNull { it.actualWeight }
                    val workoutDate = history.find { it.completedWorkout.id == sets.first().completedWorkoutId }?.completedWorkout?.completionDate
                    if (maxWeightInSession != null && workoutDate != null) {
                        MaxLiftDataPoint(workoutDate, maxWeightInSession)
                    } else null
                }
                .sortedBy { it.date }
            _maxLiftProgressionData.value = liftData
        }
    }


    // --- Active Workout Session and other methods from before ---
    private val _activeWorkoutExercises = mutableStateListOf<ActiveExercise>()
    val activeWorkoutExercises: List<ActiveExercise> = _activeWorkoutExercises
    private var originalWorkout: Workout? = null

    fun upsertMuscle(muscle: Muscle) = viewModelScope.launch { repository.upsertMuscle(muscle) }
    fun upsertExercise(exercise: Exercise) = viewModelScope.launch { repository.upsertExercise(exercise) }
    fun upsertWorkoutTemplate(workout: Workout, sets: List<WorkoutSet>) = viewModelScope.launch { repository.upsertWorkoutWithSets(workout, sets) }
    fun deleteWorkout(workout: Workout) = viewModelScope.launch { repository.deleteWorkout(workout) }
    fun startWorkoutSession(workoutWithSets: WorkoutWithSets) = viewModelScope.launch {
        originalWorkout = workoutWithSets.workout
        val activeExercises = mutableMapOf<String, ActiveExercise>()
        workoutWithSets.sets.forEach { workoutSet ->
            val exercise = repository.getExerciseById(workoutSet.exerciseId) ?: return@forEach
            val activeSet = ActiveWorkoutSet(workoutSet.id, exercise, workoutSet.setNumber, workoutSet.targetReps.toString(), workoutSet.targetWeight.toString(), workoutSet.targetRestInSeconds.toString())
            val existing = activeExercises[exercise.id]
            if (existing == null) activeExercises[exercise.id] = ActiveExercise(exercise, mutableListOf(activeSet))
            else existing.sets.add(activeSet)
        }
        _activeWorkoutExercises.clear()
        _activeWorkoutExercises.addAll(activeExercises.values.toList())
    }
    fun finishWorkout(durationInSeconds: Long, updateTemplate: Boolean) = viewModelScope.launch {
        val completedWorkoutId = UUID.randomUUID().toString()
        val completedWorkout = CompletedWorkout(completedWorkoutId, originalWorkout?.name ?: "Workout", Date(), TimeUnit.SECONDS.toMinutes(durationInSeconds).toInt())
        val completedSets = mutableListOf<CompletedSet>()
        val exercisesToUpdate = mutableMapOf<String, Pair<Double, Int>>()
        _activeWorkoutExercises.forEach { activeExercise ->
            activeExercise.sets.forEach { activeSet ->
                if (activeSet.isCompleted) {
                    val actualReps = activeSet.reps.toIntOrNull() ?: 0
                    val actualWeight = activeSet.weight.toDoubleOrNull() ?: 0.0
                    completedSets.add(CompletedSet(completedWorkoutId = completedWorkoutId, exerciseId = activeExercise.exercise.id, setNumber = activeSet.setNumber, actualReps = actualReps, actualWeight = actualWeight))
                    val currentRecord = exercisesToUpdate[activeExercise.exercise.id] ?: (activeExercise.exercise.maxWeight to activeExercise.exercise.maxReps)
                    if (actualWeight > currentRecord.first) exercisesToUpdate[activeExercise.exercise.id] = actualWeight to actualReps
                    else if (actualWeight == currentRecord.first && actualReps > currentRecord.second) exercisesToUpdate[activeExercise.exercise.id] = actualWeight to actualReps
                }
            }
        }
        repository.saveCompletedWorkoutSession(completedWorkout, completedSets, exercisesToUpdate)
        if (updateTemplate && originalWorkout != null) {
            val updatedWorkoutSets = _activeWorkoutExercises.flatMap { activeEx -> activeEx.sets.map { activeSet -> WorkoutSet(activeSet.workoutSetId, originalWorkout!!.id, activeEx.exercise.id, activeSet.setNumber, activeSet.reps.toIntOrNull() ?: 0, activeSet.weight.toDoubleOrNull() ?: 0.0, activeSet.rest.toIntOrNull() ?: 0) } }
            repository.upsertWorkoutWithSets(originalWorkout!!, updatedWorkoutSets)
        }
        clearActiveSession()
    }
    private fun clearActiveSession() {
        _activeWorkoutExercises.clear()
        originalWorkout = null
    }
}

class FitnessViewModelFactory(private val repository: FitnessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
