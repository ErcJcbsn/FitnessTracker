package com.example.progressiontracker
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow // <-- CORRECT IMPORT
import kotlinx.coroutines.flow.StateFlow

/**
 * Data Access Object for the Exercise entity.
 * Defines the database operations for exercises.
 */
@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>> // <-- CHANGED from StateFlow to Flow

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getExercisesByIds(ids: List<String>): List<Exercise>
}

/**
 * Data Access Object for the Workout entity.
 * Defines the database operations for workout plans.
 */
@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Query("SELECT * FROM workouts ORDER BY name ASC")
    fun getAllWorkouts(): Flow<List<Workout>> // <-- CHANGED from StateFlow to Flow
}

/**
 * Data Access Object for the CompletedWorkout entity.
 * Defines the database operations for workout history records.
 */
@Dao
interface CompletedWorkoutDao {
    @Insert
    suspend fun insertCompletedWorkout(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts ORDER BY completionDate DESC")
    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkout>> // <-- CHANGED from StateFlow to Flow
}
