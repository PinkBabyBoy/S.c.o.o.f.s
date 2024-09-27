package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.core.topBarHeaderStyle
import ru.barinov.file_browser.R
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.sideEffects.ContainersSideEffect
import ru.barinov.file_browser.states.ContainersUiState
import ru.barinov.file_browser.states.FileBrowserUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Containers(
    state: ContainersUiState,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sideEffects: Flow<ContainersSideEffect>,
    onEvent: (ContainersEvent) -> Unit
) {
    val exitConfirmDialogVisible = remember { mutableStateOf(false) }
    val isContainerCreateBsExpanded = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        exitConfirmDialogVisible.value = true
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
                coroutine = coroutineScope
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
                                    navController.navigate("${FileBrowserRout.CONTAINER_CONTENT}/{${it.fileId}}")
                                else coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Can't open without key")
                                }
                            }
                        }
                    )
                }
            }
        }
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
                    } else coroutine.launch { snackbarHostState.showSnackbar("ff") }
                }
                .size(26.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}
