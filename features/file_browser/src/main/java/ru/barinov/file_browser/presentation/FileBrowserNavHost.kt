package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.barinov.core.FileId
import ru.barinov.file_browser.BrowserRout
import ru.barinov.file_browser.ContainersContent
import ru.barinov.file_browser.ImageDetails
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.viewModels.FilesLoadInitializationViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel
import ru.barinov.routes.TopDestinations

fun NavGraphBuilder.fileBrowserPager(
    navController: NavController,
    scaffoldPaddings: PaddingValues,
    snackbarHostState: SnackbarHostState,
    bottomNavBarVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    composable(route = TopDestinations.FILE_BROWSER_HOME.name) {
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
        ContainerContent(
            uiState = vm.viewState.collectAsState(),
            eventReceiver = vm::handleEvent,
            sideEffects = vm.sideEffects,
            navController = navController
        )
    }

    composable<ImageDetails> {
        val args: ImageDetails = it.toRoute()
        val vm: ImageFileDetailsViewModel =
            koinViewModel(parameters = { parametersOf(FileId.restore(args.fileId)) })
        ImageFileScreen(
            paddingValues = scaffoldPaddings,
            sideEffects = vm.sideEffects,
            navController = navController,
            state = vm.uiState.collectAsState().value,
            onEvent = vm::handleEven,
            bottomNavBarVisibility = bottomNavBarVisibility
        )
    }

    dialog(route = BrowserRout.ENCRYPTION_START_BOTTOM_SHEET.name) {
        val vm: FilesLoadInitializationViewModel = koinViewModel()
        FileEncryptionStart(vm.sideEffects, vm.uiState, navController, vm::onEvent)
    }

    dialog(route = BrowserRout.CREATE_KEYSTORE_BOTTOM_SHEET.name) {
        CreateKeyStoreBottomSheet()
    }

    dialog(route = BrowserRout.LOAD_KEYSTORE_BOTTOM_SHEET.name) {
        KeyStoreLoadBottomSheet()
    }

    dialog(route = BrowserRout.CREATE_CONTAINER_BOTTOM_SHEET.name) {
        CreateContainerBottomSheet()
    }
}
