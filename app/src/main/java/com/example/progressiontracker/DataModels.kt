package com.example.progressiontracker

import androidx.room.*
import java.util.Date

// The Converters class remains the same as it's a general utility.
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(",").map { it.trim() }

    @TypeConverter
    fun toStringList(list: List<String>?): String? = list?.joinToString(",")
}

// --- DATABASE ENTITIES (TABLES) ---

@Entity(tableName = "muscles")
data class Muscle(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val overallMuscleGroup: String,
    val isUserAdded: Boolean = false
)

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class Exercise(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    // These lists will store the IDs of the muscles from the 'muscles' table.
    val primaryMuscleIds: List<String>,
    val secondaryMuscleIds: List<String>,
    val tertiaryMuscleIds: List<String>,
    // Personal records for this exercise
    val maxWeight: Double = 0.0,
    val maxReps: Int = 0
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(entity = Workout::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutSet(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val workoutId: String,
    val exerciseId: String,
    val setNumber: Int,
    val targetReps: Int,
    val targetWeight: Double,
    val targetRestInSeconds: Int
)

@Entity(tableName = "completed_workouts")
@TypeConverters(Converters::class)
data class CompletedWorkout(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val workoutName: String,
    val completionDate: Date,
    val durationInMinutes: Int
)

@Entity(
    tableName = "completed_sets",
    foreignKeys = [
        ForeignKey(entity = CompletedWorkout::class, parentColumns = ["id"], childColumns = ["completedWorkoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("completedWorkoutId"), Index("exerciseId")]
)
data class CompletedSet(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val completedWorkoutId: String,
    val exerciseId: String?,
    val setNumber: Int,
    val actualReps: Int,
    val actualWeight: Double
)


// --- RELATIONAL DATA CLASSES (For Queries) ---

data class WorkoutWithSets(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val sets: List<WorkoutSet>
)

data class CompletedWorkoutWithSets(
    @Embedded val completedWorkout: CompletedWorkout,
    @Relation(
        parentColumn = "id",
        entityColumn = "completedWorkoutId"
    )
    val sets: List<CompletedSet>
)


// --- UI & SESSION HELPER CLASSES (Not stored in DB) ---

data class ActiveWorkoutSet(
    val workoutSetId: String,
    val exercise: Exercise,
    val setNumber: Int,
    var reps: String,
    var weight: String,
    var rest: String,
    var isCompleted: Boolean = false
)

data class ActiveExercise(
    val exercise: Exercise,
    val sets: MutableList<ActiveWorkoutSet>
)
