package ru.barinov.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ru.barinov.file_browser.presentation.fileBrowserPager

fun NavGraphBuilder.deployMainScreens(
    navController: NavController,
    scaffoldPaddings: PaddingValues,
    snackbarHostState: SnackbarHostState,
    bottomNavBarVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    fileBrowserPager(navController, scaffoldPaddings, snackbarHostState, bottomNavBarVisibility, modifier)
}
