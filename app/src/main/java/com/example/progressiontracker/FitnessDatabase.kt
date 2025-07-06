package com.example.progressiontracker
import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main Room Database class for the application.
 * This class is abstract and annotated with @Database. It lists the entities
 * and the database version.
 */
@Database(entities = [Exercise::class, Workout::class, CompletedWorkout::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

    // Abstract methods to get the DAOs for each entity.
    // Room will generate the implementation for these.
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun completedWorkoutDao(): CompletedWorkoutDao

    // The companion object allows us to create a singleton instance of the database
    // to prevent having multiple instances of the database opened at the same time.
    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(application: Application): FitnessDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this basic implementation.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
