package ru.barinov.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ru.barinov.file_browser.FileBrowserHomeScreen
import ru.barinov.file_browser.home

fun NavGraphBuilder.deployMainScreens(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    home(navController)
}