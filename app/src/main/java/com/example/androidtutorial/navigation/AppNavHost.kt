package com.example.androidtutorial.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.core.design.bookEnter
import com.example.androidtutorial.core.design.bookExit
import com.example.androidtutorial.core.design.bookPopEnter
import com.example.androidtutorial.core.design.bookPopExit
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.feature.auth.AuthScreen
import com.example.androidtutorial.feature.profile.ProfileScreen
import com.example.androidtutorial.feature.shell.MainShell
import com.example.androidtutorial.feature.topics.detail.TopicDetailScreen
import com.example.androidtutorial.feature.topics.detail.TopicDetailViewModel
import com.example.androidtutorial.feature.topics.editor.TopicEditorScreen
import com.example.androidtutorial.feature.topics.editor.TopicEditorViewModel

private const val TopicIdArg = "topicId"
private const val CategoryArg = "category"

/**
 * Pops [entry] only while it is the settled destination: a second tap or a late effect during
 * the transition would otherwise pop the shell too and leave an empty back stack.
 */
private fun NavController.popIfResumed(entry: NavBackStackEntry) {
    if (!entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
    if (currentDestination?.route == AppDestination.Shell) return
    popBackStack()
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.Auth,
        modifier = modifier.background(BookColors.Paper),
    ) {
        composable(
            route = AppDestination.Auth,
            exitTransition = { fadeOut(tween(320)) },
        ) {
            AuthScreen(
                onEntered = {
                    navController.navigate(AppDestination.Shell) {
                        popUpTo(AppDestination.Auth) { inclusive = true }
                    }
                },
            )
        }
        // The shell stays on the back stack under detail, editor and profile, so its list
        // ViewModels and visit counters survive a round trip.
        composable(
            route = AppDestination.Shell,
            enterTransition = { fadeIn(tween(320)) },
            exitTransition = { bookExit() },
            popEnterTransition = { bookPopEnter() },
        ) {
            MainShell(
                onOpenTopic = { id -> navController.navigate(AppDestination.detail(id)) },
                onAddTopic = { category -> navController.navigate(AppDestination.editor(category)) },
                onOpenProfile = { navController.navigate(AppDestination.Profile) },
            )
        }
        composable(
            route = AppDestination.Detail,
            arguments = listOf(navArgument(TopicIdArg) { type = NavType.LongType }),
            enterTransition = { bookEnter() },
            exitTransition = { bookExit() },
            popEnterTransition = { bookPopEnter() },
            popExitTransition = { bookPopExit() },
        ) { entry ->
            val topicId = entry.arguments?.getLong(TopicIdArg) ?: 0L
            val viewModel = hiltViewModel<TopicDetailViewModel, TopicDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(topicId) },
            )
            TopicDetailScreen(
                viewModel = viewModel,
                onClose = { navController.popIfResumed(entry) },
            )
        }
        composable(
            route = AppDestination.Editor,
            arguments = listOf(
                navArgument(CategoryArg) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
            enterTransition = { bookEnter() },
            exitTransition = { bookExit() },
            popEnterTransition = { bookPopEnter() },
            popExitTransition = { bookPopExit() },
        ) { entry ->
            val raw = entry.arguments?.getString(CategoryArg)
            val category = TopicCategory.entries.firstOrNull { it.name == raw }
            // One editor ViewModel per opening: it is scoped to this back stack entry.
            val viewModel = hiltViewModel<TopicEditorViewModel, TopicEditorViewModel.Factory>(
                creationCallback = { factory -> factory.create(category) },
            )
            TopicEditorScreen(
                viewModel = viewModel,
                onClose = { navController.popIfResumed(entry) },
            )
        }
        composable(
            route = AppDestination.Profile,
            enterTransition = { bookEnter() },
            exitTransition = { bookExit() },
            popEnterTransition = { bookPopEnter() },
            popExitTransition = { bookPopExit() },
        ) { entry ->
            ProfileScreen(onBack = { navController.popIfResumed(entry) })
        }
    }
}
