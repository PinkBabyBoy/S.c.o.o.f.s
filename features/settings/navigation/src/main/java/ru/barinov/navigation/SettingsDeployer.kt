package ru.barinov.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ru.barinov.settings.presentation.SettingsScreen
import ru.barinov.settings.presentation.settings

fun NavGraphBuilder.deploySettings(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    settings(navController)
}