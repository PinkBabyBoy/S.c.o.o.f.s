package ru.barinov.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ru.barinov.core.navigation.Routes
import ru.barinov.navigation.deployMainScreens
import ru.barinov.protected_enter.navigation.deployEnterFeature
import ru.barinov.routes.EnterScreenRoute
import ru.barinov.routes.FileBrowserHome

@Composable
fun ScoofNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ENTER.name
    ) {
        navigation(
            route = Routes.ENTER.name,
            startDestination = EnterScreenRoute.ENTER_SCREEN.name
        ) {
            deployEnterFeature(navController)
        }
        navigation(
            route = Routes.MAIN.name,
            startDestination = FileBrowserHome.FILE_BROWSER_HOME.name
        ) {
            deployMainScreens(navController)
        }
    }
}
