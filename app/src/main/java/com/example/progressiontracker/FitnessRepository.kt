package com.example.progressiontracker

import kotlinx.coroutines.flow.Flow

/**
 * Repository for the v4.0 app. It now includes the MuscleDao.
 *
 * @param muscleDao The DAO for the muscle databank.
 * @param exerciseDao The DAO for exercise data.
 * @param workoutDao The DAO for workout template data.
 * @param completedWorkoutDao The DAO for workout history data.
 */
class FitnessRepository(
    private val muscleDao: MuscleDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val completedWorkoutDao: CompletedWorkoutDao
) {
    // Expose Flows for observing lists of data.
    val allMuscles: Flow<List<Muscle>> = muscleDao.getAllMuscles()
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    val allWorkoutsWithSets: Flow<List<WorkoutWithSets>> = workoutDao.getAllWorkoutsWithSets()
    val completedWorkoutsHistory: Flow<List<CompletedWorkout>> = completedWorkoutDao.getCompletedWorkouts()
    // Flow to get all completed sets for volume calculation
    val allCompletedWorkoutsWithSets: Flow<List<CompletedWorkoutWithSets>> = completedWorkoutDao.getAllCompletedWorkoutsWithSets()


    // --- Muscle Methods ---
    suspend fun upsertMuscle(muscle: Muscle) {
        muscleDao.upsertMuscle(muscle)
    }

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
        exercisesToUpdate: Map<String, Pair<Double, Int>>
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
