package com.example.progressiontracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the new Muscle entity.
 */
@Dao
interface MuscleDao {
    @Upsert
    suspend fun upsertMuscle(muscle: Muscle)

    @Query("SELECT * FROM muscles ORDER BY name ASC")
    fun getAllMuscles(): Flow<List<Muscle>>
}

/**
 * Data Access Object for the updated Exercise entity.
 */
@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): Exercise?

    @Query("UPDATE exercises SET maxWeight = :newMaxWeight, maxReps = :newMaxReps WHERE id = :exerciseId")
    suspend fun updateExerciseRecords(exerciseId: String, newMaxWeight: Double, newMaxReps: Int)
}

/**
 * Data Access Object for Workout templates. Unchanged from v2.0.
 */
@Dao
interface WorkoutDao {
    @Upsert
    suspend fun upsertWorkout(workout: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSet>)

    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun deleteWorkoutSetsByWorkoutId(workoutId: String)

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithSets(workoutId: String): WorkoutWithSets?

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY name ASC")
    fun getAllWorkoutsWithSets(): Flow<List<WorkoutWithSets>>

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}

/**
 * Data Access Object for workout history. Unchanged from v2.0.
 */
@Dao
interface CompletedWorkoutDao {
    @Insert
    suspend fun insertCompletedWorkout(completedWorkout: CompletedWorkout)

    @Insert
    suspend fun insertCompletedSets(sets: List<CompletedSet>)

    @Transaction
    @Query("SELECT * FROM completed_workouts ORDER BY completionDate DESC")
    fun getAllCompletedWorkoutsWithSets(): Flow<List<CompletedWorkoutWithSets>>

    @Query("SELECT * FROM completed_workouts ORDER BY completionDate DESC")
    fun getCompletedWorkouts(): Flow<List<CompletedWorkout>>
}
