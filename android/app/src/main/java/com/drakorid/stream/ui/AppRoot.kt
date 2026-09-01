package com.drakorid.stream.ui.AppRoot

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.drakorid.stream.data.download.DownloadScheduler
import com.drakorid.stream.data.local.HistoryRepository
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.ui.downloads.DownloadsScreen
import com.drakorid.stream.ui.history.HistoryScreen
import com.drakorid.stream.ui.home.HomeScreen
import com.drakorid.stream.ui.player.PlayerScreen
import com.drakorid.stream.ui.search.SearchScreen
import com.drakorid.stream.ui.category.CategoryListScreen
import com.drakorid.stream.ui.category.CategoryDetailScreen
import com.drakorid.stream.ui.detail.DramaDetailScreen

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Categories : Screen("categories", "Categories", Icons.Default.List)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)
}

private val bottomBarScreens = listOf(Screen.Home, Screen.Search, Screen.Categories, Screen.History, Screen.Downloads)

@Composable
fun AppRoot(
    repository: DramaRepository,
    historyRepo: HistoryRepository,
    downloadScheduler: DownloadScheduler,
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination
    val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) NavigationBar {
                bottomBarScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onDramaClick = { slug -> navController.navigate("detail/$slug") },
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    repository = repository,
                    onDramaClick = { slug -> navController.navigate("detail/$slug") },
                )
            }
            composable(Screen.Categories.route) {
                CategoryListScreen(
                    repository = repository,
                    onCategoryClick = { slug, name ->
                        navController.navigate("category/$slug/$name")
                    },
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    historyRepo = historyRepo,
                    onDramaClick = { slug -> navController.navigate("detail/$slug") },
                )
            }
            composable(Screen.Downloads.route) {
                DownloadsScreen(downloadScheduler = downloadScheduler)
            }

            // Drama detail
            composable(
                "detail/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { backStack ->
                val slug = backStack.arguments?.getString("slug").orEmpty()
                DramaDetailScreen(
                    repository = repository,
                    slug = slug,
                    onEpisodeClick = { s, ep -> navController.navigate("player/$s/$ep") },
                )
            }

            // Category detail
            composable(
                "category/{slug}/{name}",
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                ),
            ) { backStack ->
                CategoryDetailScreen(
                    repository = repository,
                    slug = backStack.arguments?.getString("slug").orEmpty(),
                    name = backStack.arguments?.getString("name").orEmpty(),
                    onDramaClick = { slug -> navController.navigate("detail/$slug") },
                )
            }

            // Player
            composable(
                "player/{slug}/{episode}",
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("episode") { type = NavType.IntType },
                ),
            ) { backStack ->
                PlayerScreen(
                    repository = repository,
                    historyRepo = historyRepo,
                    dramaId = 0,
                    slug = backStack.arguments?.getString("slug").orEmpty(),
                    episodeNumber = backStack.arguments?.getInt("episode") ?: 1,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}