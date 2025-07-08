package com.example.progressiontracker

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main Room Database class for the v4.0 application.
 */
@Database(
    entities = [
        Muscle::class, // <-- Added new Muscle entity
        Exercise::class,
        Workout::class,
        WorkoutSet::class,
        CompletedWorkout::class,
        CompletedSet::class
    ],
    version = 3, // <-- THE FIX: Version number is now 3 to reflect the new schema
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

    // Add an abstract method for the new MuscleDao
    abstract fun muscleDao(): MuscleDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun completedWorkoutDao(): CompletedWorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(application: Application): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    // Wipes and rebuilds the database on version change.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
