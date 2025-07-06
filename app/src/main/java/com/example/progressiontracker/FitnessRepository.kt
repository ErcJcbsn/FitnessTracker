package com.example.progressiontracker
import kotlinx.coroutines.flow.Flow

/**
 * A repository class that abstracts access to multiple data sources.
 * It is the single source of truth for the application's data.
 * It provides a clean API for data access to the rest of the application.
 *
 * @param exerciseDao The DAO for exercise data.
 * @param workoutDao The DAO for workout data.
 * @param completedWorkoutDao The DAO for completed workout history data.
 */
class FitnessRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val completedWorkoutDao: CompletedWorkoutDao
) {
    // Expose the Flows from the DAOs. The ViewModel will convert these to StateFlows.
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()
    val allCompletedWorkouts: Flow<List<CompletedWorkout>> = completedWorkoutDao.getAllCompletedWorkouts()

    // Suspend functions that call the corresponding DAO methods.
    // These will be called from the ViewModel's coroutine scope.
    suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }

    suspend fun addWorkout(workout: Workout) {
        workoutDao.insertWorkout(workout)
    }

    suspend fun addCompletedWorkout(completedWorkout: CompletedWorkout) {
        completedWorkoutDao.insertCompletedWorkout(completedWorkout)
    }

    suspend fun getExercisesForWorkout(workout: Workout): List<Exercise> {
        return exerciseDao.getExercisesByIds(workout.exerciseIds)
    }
}
