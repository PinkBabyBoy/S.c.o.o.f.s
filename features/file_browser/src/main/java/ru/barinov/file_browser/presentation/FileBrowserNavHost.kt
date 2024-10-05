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
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.file_browser.ContainersContent
import ru.barinov.file_browser.ImageDetails
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.viewModels.ContainersViewModel
import ru.barinov.file_browser.viewModels.FileObserverViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel
import ru.barinov.file_browser.viewModels.KeySelectorViewModel

@Composable
fun FileBrowserNavHost(
    navController: NavHostController,
    startDestination: String,
    scaffoldPaddings: PaddingValues,
    snackbarHostState: SnackbarHostState,
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
            val vm: ContainersViewModel = koinViewModel()
            Containers(
                state = vm.uiState.collectAsState().value,
                navController = navController,
                snackbarHostState = snackbarHostState,
                sideEffects = vm.sideEffects,
                onEvent = vm::handleEvent
            )
        }

        composable<ContainersContent> {
            val args: ContainersContent = it.toRoute()
            val vm: ContainerContentViewModel = koinViewModel(parameters = { parametersOf(FileId.restore(args.fileId)) })
            ContainerContent()
        }

        composable<ImageDetails> {
            val args: ImageDetails = it.toRoute()
            val vm: ImageFileDetailsViewModel = koinViewModel(parameters = { parametersOf(FileId.restore(args.fileId), args.source) })
            ImageFileScreen(navController, vm.uiState.collectAsState().value)
        }

        composable(
            route = FileBrowserRout.FILE_PICKER.name,
            enterTransition = {
                enterSlider(
                    initialState.destination.route,
                    FileBrowserRout.FILE_PICKER.name
                )
            },
            exitTransition = {
                exitSlider(
                    initialState.destination.route,
                    FileBrowserRout.CONTAINERS.name
                )
            }
        ) {
            val vm: FileObserverViewModel = koinViewModel()
            FileBrowserScreen(
                state = vm.uiState.collectAsState().value,
                scaffoldPaddingValues = scaffoldPaddings,
                sideEffects = vm.sideEffects,
                navController = navController,
                onEvent = vm::onNewEvent,
                snackbarHostState = snackbarHostState
            )
        }

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
            }
        ) {
            val vm: KeySelectorViewModel = koinViewModel()
            KeySelector(
                state = vm.uiState.collectAsState().value,
                scaffoldPaddings = scaffoldPaddings,
                onEvent = vm::handleEvent,
                sideEffects = vm.sideEffects,
                navController = navController,
                snackbarHostState = snackbarHostState

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
