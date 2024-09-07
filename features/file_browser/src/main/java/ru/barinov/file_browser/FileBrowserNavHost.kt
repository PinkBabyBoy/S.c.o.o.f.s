package ru.barinov.file_browser

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun FileBrowserNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = FileBrowserRout.CONTAINERS.name, enterTransition = {
            enterSlider(initialState.destination.route, FileBrowserRout.CONTAINERS.name)
        },
            exitTransition = {
                exitSlider(initialState.destination.route, FileBrowserRout.CONTAINERS.name)
            }
        ) {
            Containers()
        }
        composable(
            route = FileBrowserRout.FILE_PICKER.name,
            enterTransition = {
                enterSlider(
                    initialState.destination.route,
                    FileBrowserRout.FILE_PICKER.name
                )
            }, exitTransition = { exitSlider(initialState.destination.route, FileBrowserRout.CONTAINERS.name) }
        ) { FilePickerScreen() }

        composable(
            route = FileBrowserRout.KEY_PICKER.name,
            enterTransition = {
                enterSlider(
                    initialState.destination.route,
                    FileBrowserRout.KEY_PICKER.name
                )
            },
            exitTransition = {
                exitSlider(
                    initialState.destination.route,
                    FileBrowserRout.CONTAINERS.name
                )
            }) {
            KeyPicker()
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlider(
    fromScreen: String?,
    openScreen: String
): EnterTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == FileBrowserRout.CONTAINERS.name -> AnimatedContentTransitionScope.SlideDirection.Right
        openScreen == FileBrowserRout.KEY_PICKER.name -> AnimatedContentTransitionScope.SlideDirection.Left
        openScreen == FileBrowserRout.FILE_PICKER.name && fromScreen == FileBrowserRout.CONTAINERS.name
        -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> AnimatedContentTransitionScope.SlideDirection.Right
    }
    return slideIntoContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitSlider(
    fromScreen: String?,
    openScreen: String
): ExitTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == FileBrowserRout.CONTAINERS.name && fromScreen != FileBrowserRout.CONTAINERS.name
        -> AnimatedContentTransitionScope.SlideDirection.Right
        openScreen == FileBrowserRout.KEY_PICKER.name -> AnimatedContentTransitionScope.SlideDirection.Left
        openScreen == FileBrowserRout.FILE_PICKER.name && fromScreen == FileBrowserRout.CONTAINERS.name
        -> AnimatedContentTransitionScope.SlideDirection.Right

        else -> AnimatedContentTransitionScope.SlideDirection.Left
    }
    return slideOutOfContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}
