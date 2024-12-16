package ru.barinov.file_browser.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.barinov.core.FileId
import ru.barinov.file_browser.ContainersContent
import ru.barinov.file_browser.ImageDetails
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel

@Composable
fun FileBrowserNavHost(
    navController: NavHostController,
    startDestination: String,
    scaffoldPaddings: PaddingValues,
    snackbarHostState: SnackbarHostState,
    bottomNavBarVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = FileBrowserRout.FILE_OBSERVER.name, enterTransition = {
        enterSlider(initialState.destination.route, FileBrowserRout.SETTINGS.name)
        },
            exitTransition = {
                exitSlider(initialState.destination.route, FileBrowserRout.FILE_OBSERVER.name)
            }
        ) {
            HostPager(
                navController = navController,
                snackbarHostState = snackbarHostState,
                scaffoldPaddings = scaffoldPaddings
            )
        }

        composable<ContainersContent> {
            val args: ContainersContent = it.toRoute()
            val vm: ContainerContentViewModel =
                koinViewModel(parameters = { parametersOf(args.fileId) })
            ContainerContent()
        }

        composable<ImageDetails> {
            val args: ImageDetails = it.toRoute()
            val vm: ImageFileDetailsViewModel =
                koinViewModel(parameters = { parametersOf(FileId.restore(args.fileId), args.source) })
            ImageFileScreen(
                paddingValues = scaffoldPaddings,
                sideEffects = vm.sideEffects,
                navController = navController,
                state = vm.uiState.collectAsState().value,
                onEvent = vm::handleEven,
                bottomNavBarVisibility = bottomNavBarVisibility
            )
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlider(
    fromScreen: String?,
    openScreen: String
): EnterTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == FileBrowserRout.FILE_OBSERVER.name -> AnimatedContentTransitionScope.SlideDirection.Right
        openScreen == FileBrowserRout.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

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
        openScreen == FileBrowserRout.FILE_OBSERVER.name && fromScreen != FileBrowserRout.FILE_OBSERVER.name
        -> AnimatedContentTransitionScope.SlideDirection.Right

        openScreen == FileBrowserRout.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> AnimatedContentTransitionScope.SlideDirection.Left
    }
    return slideOutOfContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}
