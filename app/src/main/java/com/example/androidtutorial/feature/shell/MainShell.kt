package com.example.androidtutorial.feature.shell

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter
import com.example.androidtutorial.feature.study.StudyScreen
import com.example.androidtutorial.feature.topics.list.TopicListEffect
import com.example.androidtutorial.feature.topics.list.TopicListIntent
import com.example.androidtutorial.feature.topics.list.TopicListScreen
import com.example.androidtutorial.feature.topics.list.TopicListViewModel
import com.example.androidtutorial.navigation.AppDestination
import kotlinx.coroutines.launch

private data class Tab(val route: String, val title: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(AppDestination.Home, "Главная", Icons.Outlined.Home),
    Tab(AppDestination.Kotlin, "Kotlin", Icons.Outlined.Code),
    Tab(AppDestination.Android, "Android", Icons.Outlined.Android),
    Tab(AppDestination.Study, "Изучать", Icons.Outlined.School),
    Tab(AppDestination.Favorites, "Избранное", Icons.Outlined.FavoriteBorder),
)

/** Visit number per tab route. Survives the shell leaving composition under a detail screen. */
private class TabVisits {
    val counts = mutableStateMapOf<String, Int>()

    fun of(route: String): Int = counts[route] ?: 0

    fun increment(route: String) {
        counts[route] = of(route) + 1
    }

    companion object {
        val Saver = listSaver<TabVisits, Any>(
            save = { visits -> visits.counts.flatMap { listOf(it.key, it.value) } },
            restore = { saved ->
                TabVisits().also { visits ->
                    saved.chunked(2).forEach { (route, count) ->
                        visits.counts[route as String] = count as Int
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    onOpenTopic: (Long) -> Unit,
    onAddTopic: (TopicCategory?) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabNav = rememberNavController()
    val backStackEntry by tabNav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: AppDestination.Home
    val visits = rememberSaveable(saver = TabVisits.Saver) { TabVisits() }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentTab = tabs.firstOrNull { it.route == route } ?: tabs.first()
    val titleColor = when (route) {
        AppDestination.Kotlin -> BookColors.KotlinTitle
        AppDestination.Android -> BookColors.AndroidTitle
        else -> BookColors.Ink
    }
    val wash = when (route) {
        AppDestination.Kotlin -> BookColors.KotlinWash
        AppDestination.Android -> BookColors.AndroidWash
        else -> null
    }

    val onListEffect: (TopicListViewModel, TopicListEffect) -> Unit = { viewModel, effect ->
        when (effect) {
            is TopicListEffect.ShowUndo -> scope.launch {
                val result = snackbarHost.showSnackbar(
                    message = "Тема удалена",
                    actionLabel = "Отменить",
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onIntent(TopicListIntent.UndoDelete)
                }
            }

            TopicListEffect.ShowError -> scope.launch {
                snackbarHost.showSnackbar(
                    message = "Не получилось сохранить",
                    duration = SnackbarDuration.Short,
                )
                viewModel.onIntent(TopicListIntent.DismissError)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = BookColors.Paper,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            Box {
                if (wash != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(Brush.verticalGradient(listOf(wash, BookColors.Paper))),
                    )
                }
                TopAppBar(
                    title = {
                        Text(
                            text = currentTab.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = titleColor,
                        )
                    },
                    actions = {
                        IconButton(onClick = onOpenProfile) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Профиль",
                                tint = BookColors.Ink,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = BookColors.Card) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            if (route != tab.route) {
                                // Only a tab switch starts a new visit. Popping back from detail does not.
                                visits.increment(tab.route)
                                tabNav.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppDestination.Home) { saveState = true }
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BookColors.Primary,
                            selectedTextColor = BookColors.Primary,
                            indicatorColor = BookColors.KotlinBadge,
                            unselectedIconColor = BookColors.InkSecondary,
                            unselectedTextColor = BookColors.InkSecondary,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = tabNav,
            startDestination = AppDestination.Home,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { fadeOut(tween(150)) },
        ) {
            composable(AppDestination.Home) {
                val viewModel = topicListViewModel(TopicFilter.ALL)
                TopicListScreen(
                    viewModel = viewModel,
                    visit = visits.of(AppDestination.Home),
                    showAdd = true,
                    addCategory = null,
                    emptyText = null,
                    onOpenTopic = onOpenTopic,
                    onAdd = onAddTopic,
                    onEffect = onListEffect,
                )
            }
            composable(AppDestination.Kotlin) {
                val viewModel = topicListViewModel(TopicFilter.KOTLIN)
                TopicListScreen(
                    viewModel = viewModel,
                    visit = visits.of(AppDestination.Kotlin),
                    showAdd = true,
                    addCategory = TopicCategory.KOTLIN,
                    emptyText = null,
                    onOpenTopic = onOpenTopic,
                    onAdd = onAddTopic,
                    onEffect = onListEffect,
                )
            }
            composable(AppDestination.Android) {
                val viewModel = topicListViewModel(TopicFilter.ANDROID)
                TopicListScreen(
                    viewModel = viewModel,
                    visit = visits.of(AppDestination.Android),
                    showAdd = true,
                    addCategory = TopicCategory.ANDROID,
                    emptyText = null,
                    onOpenTopic = onOpenTopic,
                    onAdd = onAddTopic,
                    onEffect = onListEffect,
                )
            }
            composable(AppDestination.Study) { StudyScreen() }
            composable(AppDestination.Favorites) {
                val viewModel = topicListViewModel(TopicFilter.FAVORITES)
                TopicListScreen(
                    viewModel = viewModel,
                    visit = visits.of(AppDestination.Favorites),
                    showAdd = false,
                    addCategory = null,
                    emptyText = "В книге пока нет закладок",
                    onOpenTopic = onOpenTopic,
                    onAdd = onAddTopic,
                    onEffect = onListEffect,
                )
            }
        }
    }
}

@Composable
private fun topicListViewModel(filter: TopicFilter): TopicListViewModel =
    hiltViewModel<TopicListViewModel, TopicListViewModel.Factory>(
        creationCallback = { factory -> factory.create(filter) },
    )
