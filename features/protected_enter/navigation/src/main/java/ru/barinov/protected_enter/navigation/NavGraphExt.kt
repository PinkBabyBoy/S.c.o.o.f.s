package ru.barinov.protected_enter.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ru.barinov.protected_enter.enterScreen
import ru.barinov.protected_enter.permissionInfoScreen

fun NavGraphBuilder.deployEnterFeature(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    enterScreen(navController, modifier)
    permissionInfoScreen()
}