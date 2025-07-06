package com.example.progressiontracker
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

/**
 * A utility class to convert complex data types (like Lists and Maps) into a format
 * that can be stored in the Room database.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        // Handles empty strings from the database correctly
        return if (value.isNullOrEmpty()) listOf() else value.split(",").map { it.trim() }
    }



    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromIntList(value: String?): List<Int>? {
        // Handles empty strings from the database correctly
        return if (value.isNullOrEmpty()) listOf() else value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromStringDoubleMap(value: String?): Map<String, Double>? {
        if (value.isNullOrEmpty()) return mapOf()
        return value.split(",").associate {
            val parts = it.split("=")
            if (parts.size == 2) {
                parts[0] to parts[1].toDouble()
            } else {
                // Handle potential malformed data, though this should be rare
                "" to 0.0
            }
        }.filterKeys { it.isNotEmpty() }
    }

    @TypeConverter
    fun toStringDoubleMap(map: Map<String, Double>?): String? {
        return map?.map { "${it.key}=${it.value}" }?.joinToString(",")
    }
}


/**
 * Represents a single exercise defined by the user.
 * The @Entity annotation marks this as a table for the Room database.
 */
@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class Exercise(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val sets: Int,
    val reps: List<Int>,
    val weight: Double,
    val overallMuscleGroup: String,
    val muscleGroups: List<String>
)

/**
 * Represents a workout plan created by the user, consisting of a collection of exercises.
 */
@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class Workout(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val exerciseIds: List<String>,
    val restTimeInSeconds: Int
)

/**
 * Represents a single, completed workout session. This is the historical record.
 */
@Entity(tableName = "completed_workouts")
@TypeConverters(Converters::class)
data class CompletedWorkout(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val workoutName: String,
    val completionDate: Date,
    val durationInMinutes: Int,
    val volumeByMuscleGroup: Map<String, Double>,
    val volumeByOverallMuscleGroup: Map<String, Double>
)

/**
 * A helper data class to hold the combined details of a workout and its exercises for the UI.
 * This is not stored in the database.
 */
data class WorkoutSessionDetails(
    val workout: Workout,
    val exercises: List<Exercise>
)

