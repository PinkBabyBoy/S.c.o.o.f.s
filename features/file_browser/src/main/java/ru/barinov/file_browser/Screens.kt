package ru.barinov.file_browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.barinov.routes.FileBrowserHome

fun NavGraphBuilder.home(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    composable(route = FileBrowserHome.FILE_BROWSER_HOME.name) {
        FileBrowserHomeScreen(mainController = navController)
    }
}

