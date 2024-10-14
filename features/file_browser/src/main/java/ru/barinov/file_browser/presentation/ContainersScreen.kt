package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.sideEffects.ContainersSideEffect
import ru.barinov.file_browser.states.ContainersUiState
import ru.barinov.file_browser.toContainerContent
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.getActivity
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Containers(
    state: ContainersUiState,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sideEffects: Flow<ContainersSideEffect>,
    onEvent: (ContainersEvent) -> Unit
) {
    val context = LocalContext.current
    val exitConfirmDialogVisible = remember { mutableStateOf(false) }
    val isContainerCreateBsExpanded = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        exitConfirmDialogVisible.value = true
    }

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            ContainersSideEffect.ContainerCreated -> isContainerCreateBsExpanded.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFFFD))
    ) {
        val page = state.containers.collectAsLazyPagingItems()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        FileBrowserAppBar(
            titleString = "Containers",
            topAppBarScrollBehavior = scrollBehavior,
            onNavigateUpClicked = {},
            showArrow = false,
            actions = buildActions(
                state = state,
                onEvent = {},
                isContainerCreateBsExpanded = isContainerCreateBsExpanded,
                snackbarHostState = snackbarHostState,
                coroutine = coroutineScope,
                snackbarText = stringResource(id = ru.barinov.core.R.string.key_not_loaded_containers)
            )
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
                                    snackbarHostState.showSnackbar("Can't open without key")
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

    if (isContainerCreateBsExpanded.value) {
        CreateContainerBottomSheet(
            onDismissRequested = { isContainerCreateBsExpanded.value = false },
            onConfirmed = { onEvent(ContainersEvent.ContainerCreateConfirmed(it)) }
        )
    }
}

private fun buildActions(
    state: ContainersUiState,
    onEvent: (ContainersEvent) -> Unit,
    isContainerCreateBsExpanded: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
    snackbarText: String,
    coroutine: CoroutineScope
): Set<@Composable (RowScope) -> Unit> = buildSet {
    add {
        Icon(
            painter = painterResource(id = ru.barinov.core.R.drawable.baseline_post_add_24),
            contentDescription = null,
            modifier = Modifier
                .clickable {
                    if (state.isKeyLoaded) {
                        isContainerCreateBsExpanded.value = true
                    } else coroutine.launch { snackbarHostState.showSnackbar(snackbarText) }
                }
                .size(26.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}
