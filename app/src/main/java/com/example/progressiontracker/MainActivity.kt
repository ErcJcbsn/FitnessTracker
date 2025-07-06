package com.example.progressiontracker
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * The main entry point of the application.
 * This activity hosts all the composable screens and sets up the navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lazy initialization of the database and repository
        val database by lazy { FitnessDatabase.getDatabase(application) }
        val repository by lazy { FitnessRepository(database.exerciseDao(), database.workoutDao(), database.completedWorkoutDao()) }
        val viewModelFactory = FitnessViewModelFactory(repository)

        setContent {
            // Assuming a default MaterialTheme is set up in a Theme.kt file
            MaterialTheme {
                // Initialize the ViewModel using the factory
                val viewModel: FitnessViewModel = viewModel(factory = viewModelFactory)
                // Set up the navigation graph for the entire app
                FitnessAppNavigation(viewModel)
            }
        }
    }
}

/**
 * Composable function that defines the navigation graph for the app.
 * It controls which screen is shown based on the current route.
 *
 * @param viewModel The main ViewModel instance shared across all screens.
 */
@Composable
fun FitnessAppNavigation(viewModel: FitnessViewModel) {
    val navController = rememberNavController()

    // Collect states from the ViewModel. Compose will automatically recompose
    // when these state values change.
    val exercises by viewModel.allExercises.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val history by viewModel.workoutHistory.collectAsState()
    val activeSession by viewModel.activeWorkoutSession

    // A temporary state to hold the results from the active workout screen
    // before passing them to the summary screen.
    var workoutResultForSummary by remember { mutableStateOf<Pair<Long, Map<String, List<Boolean>>>?>(null) }

    // NavHost is the container for all navigation destinations.
    NavHost(navController = navController, startDestination = "home") {

        // Home Screen
        composable("home") {
            HomeScreen(
                workouts = workouts,
                onStartWorkout = { workout ->
                    viewModel.prepareWorkoutSession(workout)
                    navController.navigate("active_workout")
                },
                onNavigateToWorkoutCreation = { navController.navigate("workout_creation") },
                onNavigateToExerciseLibrary = { navController.navigate("exercise_library") },
                onNavigateToHistory = { navController.navigate("history") }
            )
        }

        // Exercise Library Screen
        composable("exercise_library") {
            ExerciseLibraryScreen(
                exercises = exercises,
                onAddExercise = { navController.navigate("exercise_creation") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Exercise Creation Screen
        composable("exercise_creation") {
            ExerciseCreationScreen(
                onSaveExercise = { exercise ->
                    viewModel.addExercise(exercise)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Workout Creation Screen
        composable("workout_creation") {
            WorkoutCreationScreen(
                allExercises = exercises,
                onSaveWorkout = { workout ->
                    viewModel.addWorkout(workout)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Active Workout Screen
        composable("active_workout") {
            activeSession?.let { session ->
                ActiveWorkoutScreen(
                    sessionDetails = session,
                    onFinishWorkout = { duration, completedSets ->
                        workoutResultForSummary = duration to completedSets
                        navController.navigate("summary") {
                            // Clear the back stack up to home so the user can't go back to the workout
                            popUpTo("home")
                        }
                    },
                    onNavigateBack = {
                        viewModel.clearWorkoutSession()
                        navController.popBackStack()
                    }
                )
            }
        }

        // Workout Summary Screen
        composable("summary") {
            val session = activeSession
            val result = workoutResultForSummary
            if (session != null && result != null) {
                WorkoutSummaryScreen(
                    sessionDetails = session,
                    totalDurationInSeconds = result.first,
                    completedSets = result.second,
                    onSaveAndFinish = { completedWorkout ->
                        viewModel.saveCompletedWorkout(completedWorkout)
                        viewModel.clearWorkoutSession()
                        navController.navigate("home") {
                            // Go back to home and clear the history so the user can't go back to the summary
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }

        // Workout History Screen
        composable("history") {
            WorkoutHistoryScreen(
                history = history,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
