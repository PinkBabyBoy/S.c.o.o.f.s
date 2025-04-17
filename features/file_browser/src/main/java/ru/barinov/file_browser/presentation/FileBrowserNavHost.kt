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
import ru.barinov.file_browser.ContainersContent
import ru.barinov.file_browser.CreateKeyStore
import ru.barinov.file_browser.ImageDetails
import ru.barinov.file_browser.LoadKeyStore
import ru.barinov.file_browser.NoArgsRouts
import ru.barinov.file_browser.presentation.dialogs.CreateContainerBottomSheet
import ru.barinov.file_browser.presentation.dialogs.CreateKeyStoreBottomSheet
import ru.barinov.file_browser.presentation.dialogs.FileEncryptionStart
import ru.barinov.file_browser.presentation.dialogs.KeyStoreLoadBottomSheet
import ru.barinov.file_browser.presentation.screens.ContainerContent
import ru.barinov.file_browser.presentation.screens.ImageFileScreen
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.viewModels.CreateContainerViewModel
import ru.barinov.file_browser.viewModels.FilesEncryptionInitializationViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel
import ru.barinov.file_browser.viewModels.KeyStoreCreateViewModel
import ru.barinov.file_browser.viewModels.KeyStoreLoadViewModel
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

    dialog(route = NoArgsRouts.ENCRYPTION_START_BOTTOM_SHEET.name) {
        val vm: FilesEncryptionInitializationViewModel = koinViewModel()
        FileEncryptionStart(vm.sideEffects, vm.uiState, navController, vm::onEvent)
    }

    dialog<CreateKeyStore> {
        val data: CreateKeyStore = it.toRoute()
        val vm: KeyStoreCreateViewModel = koinViewModel(parameters = { parametersOf(data.source) })
        CreateKeyStoreBottomSheet(
            navController,
            vm.sideEffects,
            vm::handleEvent
        )
    }

    dialog<LoadKeyStore> {
        val data: LoadKeyStore = it.toRoute()
        val vm: KeyStoreLoadViewModel = koinViewModel(parameters = { parametersOf(data.source) })
        KeyStoreLoadBottomSheet(
            data.filename,
            navController,
            vm.sideEffects,
            vm::handleEvent
        )
    }

    dialog(route = NoArgsRouts.CREATE_CONTAINER_BOTTOM_SHEET.name) {
        val vm: CreateContainerViewModel = koinViewModel()
        CreateContainerBottomSheet(
            navController,
            vm.sideEffects,
            vm::handleEvent
        )
    }
}
