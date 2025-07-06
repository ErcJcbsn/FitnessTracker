package com.example.progressiontracker

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main Room Database class for the application.
 * This class is abstract and annotated with @Database. It lists all the entities (tables)
 * that are part of this database and the database version.
 */
@Database(
    entities = [
        Exercise::class,
        Workout::class,
        WorkoutSet::class,
        CompletedWorkout::class,
        CompletedSet::class
    ],
    version = 2, // <-- THE FIX: Version number is now 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

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
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // This is what will be triggered by the version change.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
