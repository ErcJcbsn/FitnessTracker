package com.example.progressiontracker

import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to multiple data sources (the DAOs).
 * It is the single source of truth for the application's data.
 *
 * @param exerciseDao The DAO for exercise data.
 * @param workoutDao The DAO for workout template data.
 * @param completedWorkoutDao The DAO for workout history data.
 */
class FitnessRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val completedWorkoutDao: CompletedWorkoutDao
) {
    // Expose Flows for the UI to observe lists of data.
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    val allWorkoutsWithSets: Flow<List<WorkoutWithSets>> = workoutDao.getAllWorkoutsWithSets()
    // This now correctly uses the simple query for the history list.
    val completedWorkoutsHistory: Flow<List<CompletedWorkout>> = completedWorkoutDao.getCompletedWorkouts()

    // --- Exercise Methods ---
    suspend fun upsertExercise(exercise: Exercise) {
        exerciseDao.upsertExercise(exercise)
    }

    suspend fun getExerciseById(id: String): Exercise? {
        return exerciseDao.getExerciseById(id)
    }

    // --- Workout Template Methods ---
    suspend fun upsertWorkoutWithSets(workout: Workout, sets: List<WorkoutSet>) {
        workoutDao.upsertWorkout(workout)
        // Ensure data consistency by replacing old sets with the new list
        workoutDao.deleteWorkoutSetsByWorkoutId(workout.id)
        workoutDao.insertWorkoutSets(sets)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }

    // --- Completed Workout Methods ---
    suspend fun saveCompletedWorkoutSession(
        completedWorkout: CompletedWorkout,
        completedSets: List<CompletedSet>,
        exercisesToUpdate: Map<String, Pair<Double, Int>> // Map<ExerciseID, Pair<newMaxWeight, newMaxReps>>
    ) {
        completedWorkoutDao.insertCompletedWorkout(completedWorkout)
        completedWorkoutDao.insertCompletedSets(completedSets)

        exercisesToUpdate.forEach { (exerciseId, records) ->
            exerciseDao.updateExerciseRecords(
                exerciseId = exerciseId,
                newMaxWeight = records.first,
                newMaxReps = records.second
            )
        }
    }
}
