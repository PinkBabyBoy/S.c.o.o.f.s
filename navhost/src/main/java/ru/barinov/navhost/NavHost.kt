package ru.barinov.navhost

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import ru.barinov.core.navigation.Routes
import ru.barinov.navigation.deployMainScreens
import ru.barinov.navigation.deploySettings
import ru.barinov.protected_enter.navigation.deployEnterFeature
import ru.barinov.routes.EnterScreenRoute
import ru.barinov.routes.TopDestinations

@Composable
fun ScoofNavHost(
    navController: NavHostController,
    startDestination: String,
    scaffoldPaddings: PaddingValues,
    snackbarHostState: SnackbarHostState,
    bottomNavBarVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation(
            route = Routes.ENTER.name,
            startDestination = EnterScreenRoute.ENTER_SCREEN.name
        ) {
            bottomNavBarVisibility(false)
            deployEnterFeature(navController)
        }
        navigation(
            route = Routes.BROWSER.name,
            startDestination = TopDestinations.FILE_BROWSER_HOME.name
        ) {
            bottomNavBarVisibility(true)
            deployMainScreens(navController, scaffoldPaddings, snackbarHostState, bottomNavBarVisibility, modifier)
        }

        navigation(
            route = Routes.SETTINGS.name,
            startDestination = TopDestinations.SETTINGS.name
        ) {
            deploySettings(navController)
        }
    }
}
