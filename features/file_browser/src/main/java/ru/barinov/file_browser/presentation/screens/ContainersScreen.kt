package ru.barinov.file_browser.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.core.R
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.sideEffects.ContainersSideEffect
import ru.barinov.file_browser.states.ContainersUiState
import ru.barinov.file_browser.toContainerContent
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.getActivity
import ru.barinov.file_browser.NoArgsRouts
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.presentation.Action
import ru.barinov.file_browser.presentation.BrowserBlock
import ru.barinov.file_browser.presentation.FileBrowserAppBar
import ru.barinov.file_browser.presentation.FileItem
import ru.barinov.file_browser.presentation.Pages
import ru.barinov.file_browser.presentation.dialogs.CreateContainerBottomSheet
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Containers(
    state: ContainersUiState,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sideEffects: Flow<ContainersSideEffect>,
    onEvent: (ContainersEvent) -> Unit,
    pageState: MutableIntState,
    openPage: (Int) -> Unit
) {
    val context = LocalContext.current
    val exitConfirmDialogVisible = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isPageOnScreen = remember {
        derivedStateOf {
            pageState.intValue == Pages.CONTAINERS.ordinal
        }
    }
    BackHandler(enabled = isPageOnScreen.value) {
        exitConfirmDialogVisible.value = true
    }

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            ContainersSideEffect.OpenContainerCreateBottomSheet -> navController.navigate(NoArgsRouts.CREATE_CONTAINER_BOTTOM_SHEET.name)
            ContainersSideEffect.ShowCantCreateMessage -> {
                coroutineScope.launch {
                    val message = context.getString(R.string.key_not_loaded_containers)
                    snackbarHostState.showSnackbar(message)
                }
            }

            is ContainersSideEffect.OpenContainerDetails -> TODO()
            ContainersSideEffect.ShowCantOpenMessage -> TODO()
        }
    }
    //TODO BrowserBlock
    BrowserBlock<ContainersEvent, FileUiModel>(
        files = state.containers,
        currentFolderName = String(),
        isSelectionEnabled = false,
        onEvent = onEvent,
        isPageEmpty = state.isPageEmpty,
        isInRoot = true,
        showLoading = false,
        appbarState = state.appbarState

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFFFD))
    ) {

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        FileBrowserAppBar(
            titleString = "Containers",
            topAppBarScrollBehavior = scrollBehavior,
            onNavigateUpClicked = {},
            showArrow = false,
            appbarState = state.appBarState,
            actions = buildActions(
                state = state,
                onEvent = {},
                isContainerCreateBsExpanded = isContainerCreateBsExpanded,
                snackbarHostState = snackbarHostState,
                coroutine = coroutineScope,
                snackbarText = ,
            ),
            inOnboarding = false
        )
        LazyColumn {
            items(page.itemCount) { index ->
                val fileModel = page[index]
                if (fileModel != null) {
                    FileItem<ContainersEvent>(
                        file = fileModel,
                        selectionMode = false,
                        selectionAvailable = false,
                        onEvent = {
                            if (it is OnFileClicked) {
                                if (state.isKeyLoaded)
                                    navController.navigate(toContainerContent(it.fileId))
                                else coroutineScope.launch {
                                   val result = snackbarHostState.showSnackbar(
                                        "Can't open without key",
                                        withDismissAction = true,
                                        actionLabel = "Select key file"
                                    )
                                    when(result){
                                        SnackbarResult.Dismissed -> {}
                                        SnackbarResult.ActionPerformed -> {
                                            openPage(Pages.KEY_PICKER.ordinal)
                                        }
                                    }
                                }
                            }
                        },
                        showLoading = false,
                        additionalInfoEnabled = true
                    )
                }
            }
        }
    }

    if (exitConfirmDialogVisible.value) {
        ScoofAlertDialog(
            title = "Exit the application?",
            message = "Key should be loaded again on next start",
            onConfirmed = {
                context.getActivity()?.finish()
                if (!state.hasActiveWork)
                    exitProcess(0)
            },
            onDismissRequest = { exitConfirmDialogVisible.value = false }
        )
    }
}
