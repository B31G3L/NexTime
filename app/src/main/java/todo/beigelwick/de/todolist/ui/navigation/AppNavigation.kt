package todo.beigelwick.de.todolist.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import todo.beigelwick.de.todolist.ui.screens.addedit.AddEditScreen
import todo.beigelwick.de.todolist.ui.screens.info.InfoScreen
import todo.beigelwick.de.todolist.ui.screens.main.MainScreen
import todo.beigelwick.de.todolist.ui.screens.settings.SettingsScreen
import todo.beigelwick.de.todolist.ui.screens.welcome.WelcomeScreen
import todo.beigelwick.de.todolist.ui.screens.welcome.isWelcomeSeen

// ─── Routen ───────────────────────────────────────────────────────────────────

object Routes {
    const val WELCOME  = "welcome"
    const val MAIN     = "main"
    const val SETTINGS = "settings"
    const val INFO     = "info"

    const val ADD_EDIT     = "addedit/{id}"
    const val ADD_EDIT_NEW = "addedit/-1"

    fun addEdit(id: Long) = "addedit/$id"
}

// ─── Animations ───────────────────────────────────────────────────────────────

private const val ANIM_DURATION = 380

// ─── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // welcome_seen einmalig auslesen, um das Startziel zu bestimmen.
    // null = noch nicht geladen → nichts rendern (MainActivity zeigt den Surface-Hintergrund).
    val welcomeSeen by produceState<Boolean?>(initialValue = null) {
        value = isWelcomeSeen(context).first()
    }
    val seen = welcomeSeen ?: return

    NavHost(
        navController    = navController,
        startDestination = if (seen) Routes.MAIN else Routes.WELCOME
    ) {

        // ── Welcome / Onboarding ───────────────────────────────────────────────
        composable(
            route = Routes.WELCOME,
            exitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            }
        ) {
            WelcomeScreen(
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

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