package com.example.progressiontracker

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * The ViewModel for the v2.0 app.
 * It manages the UI state and acts as the bridge between the UI and the Repository.
 *
 * @param repository The repository that this ViewModel will get its data from.
 */
class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    // --- StateFlows for observing lists from the database ---
    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutsWithSets: StateFlow<List<WorkoutWithSets>> = repository.allWorkoutsWithSets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // This now correctly uses the simple history Flow from the repository
    val workoutHistory: StateFlow<List<CompletedWorkout>> = repository.completedWorkoutsHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State for the Active Workout Session ---
    private val _activeWorkoutExercises = mutableStateListOf<ActiveExercise>()
    val activeWorkoutExercises: List<ActiveExercise> = _activeWorkoutExercises
    private var originalWorkout: Workout? = null


    // --- Exercise Management ---
    fun upsertExercise(exercise: Exercise) = viewModelScope.launch {
        repository.upsertExercise(exercise)
    }

    // --- Workout Template Management ---
    fun upsertWorkoutTemplate(workout: Workout, sets: List<WorkoutSet>) = viewModelScope.launch {
        repository.upsertWorkoutWithSets(workout, sets)
    }

    fun deleteWorkout(workout: Workout) = viewModelScope.launch {
        repository.deleteWorkout(workout)
    }

    // --- Active Workout Session Management ---
    fun startWorkoutSession(workoutWithSets: WorkoutWithSets) = viewModelScope.launch {
        originalWorkout = workoutWithSets.workout
        val activeExercises = mutableMapOf<String, ActiveExercise>()

        // Group sets by exercise
        workoutWithSets.sets.forEach { workoutSet ->
            val exercise = repository.getExerciseById(workoutSet.exerciseId) ?: return@forEach
            val activeSet = ActiveWorkoutSet(
                workoutSetId = workoutSet.id,
                exercise = exercise,
                setNumber = workoutSet.setNumber,
                reps = workoutSet.targetReps.toString(),
                weight = workoutSet.targetWeight.toString(),
                rest = workoutSet.targetRestInSeconds.toString()
            )

            val existingActiveExercise = activeExercises[exercise.id]
            if (existingActiveExercise == null) {
                activeExercises[exercise.id] = ActiveExercise(exercise, mutableListOf(activeSet))
            } else {
                existingActiveExercise.sets.add(activeSet)
            }
        }
        _activeWorkoutExercises.clear()
        _activeWorkoutExercises.addAll(activeExercises.values.toList())
    }

    fun finishWorkout(durationInSeconds: Long, updateTemplate: Boolean) = viewModelScope.launch {
        val completedWorkoutId = java.util.UUID.randomUUID().toString()
        val completedWorkout = CompletedWorkout(
            id = completedWorkoutId,
            workoutName = originalWorkout?.name ?: "Workout",
            completionDate = Date(),
            durationInMinutes = TimeUnit.SECONDS.toMinutes(durationInSeconds).toInt()
        )

        val completedSets = mutableListOf<CompletedSet>()
        val exercisesToUpdate = mutableMapOf<String, Pair<Double, Int>>()

        _activeWorkoutExercises.forEach { activeExercise ->
            activeExercise.sets.forEach { activeSet ->
                if (activeSet.isCompleted) {
                    val actualReps = activeSet.reps.toIntOrNull() ?: 0
                    val actualWeight = activeSet.weight.toDoubleOrNull() ?: 0.0

                    completedSets.add(
                        CompletedSet(
                            completedWorkoutId = completedWorkoutId,
                            exerciseId = activeExercise.exercise.id,
                            setNumber = activeSet.setNumber,
                            actualReps = actualReps,
                            actualWeight = actualWeight
                        )
                    )

                    // Check for new personal records
                    val currentRecord = exercisesToUpdate[activeExercise.exercise.id]
                        ?: (activeExercise.exercise.maxWeight to activeExercise.exercise.maxReps)

                    if (actualWeight > currentRecord.first) {
                        exercisesToUpdate[activeExercise.exercise.id] = actualWeight to actualReps
                    } else if (actualWeight == currentRecord.first && actualReps > currentRecord.second) {
                        exercisesToUpdate[activeExercise.exercise.id] = actualWeight to actualReps
                    }
                }
            }
        }

        repository.saveCompletedWorkoutSession(completedWorkout, completedSets, exercisesToUpdate)

        if (updateTemplate && originalWorkout != null) {
            val updatedWorkoutSets = _activeWorkoutExercises.flatMap { activeExercise ->
                activeExercise.sets.map { activeSet ->
                    WorkoutSet(
                        id = activeSet.workoutSetId,
                        workoutId = originalWorkout!!.id,
                        exerciseId = activeExercise.exercise.id,
                        setNumber = activeSet.setNumber,
                        targetReps = activeSet.reps.toIntOrNull() ?: 0,
                        targetWeight = activeSet.weight.toDoubleOrNull() ?: 0.0,
                        targetRestInSeconds = activeSet.rest.toIntOrNull() ?: 0
                    )
                }
            }
            repository.upsertWorkoutWithSets(originalWorkout!!, updatedWorkoutSets)
        }
        clearActiveSession()
    }

    private fun clearActiveSession() {
        _activeWorkoutExercises.clear()
        originalWorkout = null
    }
}

/**
 * Factory for creating a FitnessViewModel with a constructor argument.
 */
class FitnessViewModelFactory(private val repository: FitnessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
