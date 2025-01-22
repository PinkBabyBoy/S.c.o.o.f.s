package ru.barinov.settings.presentation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.barinov.routes.TopDestinations

fun NavGraphBuilder.settings(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    composable(route = TopDestinations.SETTINGS_HOME.name) {
        SettingsScreen(emptyList())
    }
}