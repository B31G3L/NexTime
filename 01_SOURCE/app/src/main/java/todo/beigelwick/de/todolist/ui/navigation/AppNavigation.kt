package todo.beigelwick.de.todolist.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import todo.beigelwick.de.todolist.ui.screens.addedit.AddEditScreen
import todo.beigelwick.de.todolist.ui.screens.info.InfoScreen
import todo.beigelwick.de.todolist.ui.screens.main.MainScreen
import todo.beigelwick.de.todolist.ui.screens.settings.SettingsScreen

// ─── Routen ───────────────────────────────────────────────────────────────────

object Routes {
    const val MAIN     = "main"
    const val SETTINGS = "settings"
    const val INFO     = "info"

    // id = -1L → neuer Eintrag, sonst bestehender Countdown
    const val ADD_EDIT         = "addedit/{id}"
    const val ADD_EDIT_NEW     = "addedit/-1"

    fun addEdit(id: Long) = "addedit/$id"
}

// ─── Animations ───────────────────────────────────────────────────────────────

private const val ANIM_DURATION = 380

// ─── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.MAIN
    ) {

        // ── Hauptscreen ───────────────────────────────────────────────────────
        composable(
            route = Routes.MAIN,
            enterTransition = {
                slideIntoContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            }
        ) {
            MainScreen(
                onNavigateToAddEdit  = { navController.navigate(Routes.ADD_EDIT_NEW) },
                onNavigateToEdit     = { id -> navController.navigate(Routes.addEdit(id)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToInfo     = { navController.navigate(Routes.INFO) }
            )
        }

        // ── AddEdit-Screen ────────────────────────────────────────────────────
        composable(
            route     = Routes.ADD_EDIT,
            arguments = listOf(
                navArgument("id") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            AddEditScreen(
                countdownId = id,
                onBack      = { navController.popBackStack() }
            )
        }

        // ── Einstellungen-Screen ──────────────────────────────────────────────
        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Info-Screen ───────────────────────────────────────────────────────
        composable(
            route = Routes.INFO,
            enterTransition = {
                slideIntoContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            }
        ) {
            InfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}