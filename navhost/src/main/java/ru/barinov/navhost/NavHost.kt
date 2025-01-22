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
    changeColor: () -> Unit,
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
            deployEnterFeature(navController, bottomNavBarVisibility, changeColor)
        }
        navigation(
            route = Routes.BROWSER.name,
            startDestination = TopDestinations.FILE_BROWSER_HOME.name
        ) {
            deployMainScreens(navController, scaffoldPaddings, snackbarHostState, bottomNavBarVisibility, modifier)
        }

        navigation(
            route = Routes.SETTINGS.name,
            startDestination = TopDestinations.SETTINGS_HOME.name
        ) {
            deploySettings(navController)
        }
    }
}