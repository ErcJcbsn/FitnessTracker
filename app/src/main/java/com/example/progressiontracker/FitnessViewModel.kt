package com.example.progressiontracker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * The ViewModel for the fitness tracker app.
 * It provides data to the UI and survives configuration changes.
 * It acts as a communication center between the Repository and the UI.
 *
 * @param repository The repository that this ViewModel will get its data from.
 */
class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    // Using stateIn to convert the Flows from the repository into StateFlows
    // that are observable by the Compose UI and are lifecycle-aware.
    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<Workout>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutHistory: StateFlow<List<CompletedWorkout>> = repository.allCompletedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Private mutable state for the active workout session.
    private val _activeWorkoutSession = mutableStateOf<WorkoutSessionDetails?>(null)
    // Public immutable state to be observed by the UI.
    val activeWorkoutSession: State<WorkoutSessionDetails?> = _activeWorkoutSession

    // Functions to be called from the UI to perform actions.
    // They launch a coroutine in the viewModelScope to interact with the repository.
    fun addExercise(exercise: Exercise) = viewModelScope.launch { repository.addExercise(exercise) }

    fun addWorkout(workout: Workout) = viewModelScope.launch { repository.addWorkout(workout) }

    fun saveCompletedWorkout(completedWorkout: CompletedWorkout) = viewModelScope.launch { repository.addCompletedWorkout(completedWorkout) }

    fun prepareWorkoutSession(workout: Workout) = viewModelScope.launch {
        val exercises = repository.getExercisesForWorkout(workout)
        _activeWorkoutSession.value = WorkoutSessionDetails(workout, exercises)
    }

    fun clearWorkoutSession() {
        _activeWorkoutSession.value = null
    }
}

/**
 * A factory class for creating a FitnessViewModel instance with a constructor argument (the repository).
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
