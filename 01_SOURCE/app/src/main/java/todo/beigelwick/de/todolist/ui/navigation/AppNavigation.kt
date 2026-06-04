package todo.beigelwick.de.todolist.ui.navigation

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.map
import todo.beigelwick.de.todolist.ui.screens.addedit.AddEditScreen
import todo.beigelwick.de.todolist.ui.screens.info.InfoScreen
import todo.beigelwick.de.todolist.ui.screens.main.MainScreen
import todo.beigelwick.de.todolist.ui.screens.settings.SettingsScreen
import todo.beigelwick.de.todolist.ui.screens.simulate.SimulateScreen
import todo.beigelwick.de.todolist.ui.screens.welcome.WelcomeScreen
import todo.beigelwick.de.todolist.ui.theme.dataStore

// ─── Routen ───────────────────────────────────────────────────────────────────

object Routes {
    const val WELCOME  = "welcome"
    const val MAIN     = "main"
    const val SETTINGS = "settings"
    const val INFO     = "info"
    const val SIMULATE = "simulate"

    const val ADD_EDIT     = "addedit/{id}"
    const val ADD_EDIT_NEW = "addedit/-1"
    fun addEdit(id: Long) = "addedit/$id"
}

private const val ANIM_DURATION = 380
private val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")

// ─── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    // Startdestination: Welcome wenn noch nicht gesehen, sonst Main
    val welcomeSeen by context.dataStore.data
        .map { it[WELCOME_SEEN] ?: false }
        .collectAsState(initial = null)

    // Warten bis der Wert geladen ist (null = noch nicht bekannt)
    if (welcomeSeen == null) return

    val startDest = if (welcomeSeen == true) Routes.MAIN else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDest) {

        // ── Welcome ───────────────────────────────────────────────────────────
        composable(
            route = Routes.WELCOME,
            enterTransition = { fadeIn(tween(400)) },
            exitTransition  = { fadeOut(tween(300)) }
        ) {
            WelcomeScreen(
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Main ──────────────────────────────────────────────────────────────
        composable(
            route = Routes.MAIN,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
        ) {
            MainScreen(
                onNavigateToAddEdit  = { navController.navigate(Routes.ADD_EDIT_NEW) },
                onNavigateToEdit     = { id -> navController.navigate(Routes.addEdit(id)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToInfo     = { navController.navigate(Routes.INFO) }
            )
        }

        // ── AddEdit ───────────────────────────────────────────────────────────
        composable(
            route     = Routes.ADD_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
        ) { backStackEntry ->
            AddEditScreen(
                countdownId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onBack      = { navController.popBackStack() }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(
            route = Routes.SETTINGS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
        ) {
            SettingsScreen(
                onBack          = { navController.popBackStack() },
                onNavigateToSim = { navController.navigate(Routes.SIMULATE) },
                onNavigateToWelcome = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SETTINGS) { inclusive = false }
                    }
                }
            )
        }

        // ── Info ──────────────────────────────────────────────────────────────
        composable(
            route = Routes.INFO,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
        ) { InfoScreen(onBack = { navController.popBackStack() }) }

        // ── Simulate ──────────────────────────────────────────────────────────
        composable(
            route = Routes.SIMULATE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
        ) { SimulateScreen(onBack = { navController.popBackStack() }) }
    }
}