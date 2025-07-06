package com.example.progressiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database by lazy { FitnessDatabase.getDatabase(application) }
        val repository by lazy { FitnessRepository(database.exerciseDao(), database.workoutDao(), database.completedWorkoutDao()) }
        val viewModelFactory = FitnessViewModelFactory(repository)

        setContent {
            MaterialTheme {
                val viewModel: FitnessViewModel = viewModel(factory = viewModelFactory)
                FitnessAppNavigationV2(viewModel)
            }
        }
    }
}

@Composable
fun FitnessAppNavigationV2(viewModel: FitnessViewModel) {
    val navController = rememberNavController()
    val exercises by viewModel.allExercises.collectAsState()
    val workoutsWithSets by viewModel.allWorkoutsWithSets.collectAsState()
    val history by viewModel.workoutHistory.collectAsState()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                workouts = workoutsWithSets,
                onStartWorkout = { workout ->
                    viewModel.startWorkoutSession(workout)
                    navController.navigate("active_workout")
                },
                onNavigateToWorkoutCreation = { navController.navigate("workout_creation") },
                onNavigateToExerciseLibrary = { navController.navigate("exercise_library") },
                onNavigateToHistory = { navController.navigate("history") }
            )
        }

        composable("exercise_library") {
            ExerciseLibraryScreen(
                exercises = exercises,
                onAddExercise = { navController.navigate("exercise_creation") },
                onEditExercise = { exercise ->
                    navController.navigate("exercise_creation?exerciseId=${exercise.id}")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "exercise_creation?exerciseId={exerciseId}",
            arguments = listOf(navArgument("exerciseId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            val existingExercise = exercises.find { it.id == exerciseId }
            ExerciseCreationScreen(
                existingExercise = existingExercise,
                onSaveExercise = { exercise ->
                    viewModel.upsertExercise(exercise)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("workout_creation") {
            WorkoutCreationScreen(
                allExercises = exercises,
                onSaveWorkout = { workout, sets ->
                    viewModel.upsertWorkoutTemplate(workout, sets)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("active_workout") {
            ActiveWorkoutScreen(
                activeExercises = viewModel.activeWorkoutExercises,
                onFinishWorkout = { duration ->
                    navController.navigate("workout_summary/${duration}")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "workout_summary/{duration}",
            arguments = listOf(navArgument("duration") { type = NavType.LongType })
        ) { backStackEntry ->
            val duration = backStackEntry.arguments?.getLong("duration") ?: 0L
            WorkoutSummaryScreen(
                onSaveAndFinish = { updateTemplate ->
                    viewModel.finishWorkout(duration, updateTemplate)
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("history") {
            WorkoutHistoryScreen(
                history = history,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
