package ru.barinov.file_browser.presentation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.core.Source

import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.file_browser.toImageDetails
import ru.barinov.file_browser.viewModels.InitializationMode
import ru.barinov.core.ui.BottomSheetPolicy
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.getArgs
import ru.barinov.core.ui.shouldShow

@Composable
fun FileBrowserScreen(
    state: FileBrowserUiState,
    scaffoldPaddingValues: PaddingValues,
    sideEffects: Flow<FileBrowserSideEffect>,
    navController: NavController,
    onEvent: (FileBrowserEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val confirmBsExpanded =
        remember { mutableStateOf<BottomSheetPolicy>(BottomSheetPolicy.Collapsed) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localCoroutine = rememberCoroutineScope()
    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> navController.navigateUp()
            is ShowInfo -> localCoroutine.launch {  snackbarHostState.showSnackbar(context.getString(sideEffect.text), withDismissAction = true) }
            is FileBrowserSideEffect.OpenImageFile
            -> navController.navigate(toImageDetails(sideEffect.fileId, sideEffect.source))

            is FileBrowserSideEffect.ShowAddFilesDialog -> {
                confirmBsExpanded.value = BottomSheetPolicy.Expanded(InitializationMode.Selected(sideEffect.selectedFiles))
            }
        }
    }

    if (deleteDialogVisible.value) {
        ScoofAlertDialog(
            title = "Delete selected?",
            message = "All selected files will be removed",
            onDismissRequest = { deleteDialogVisible.value = false },
            onConfirmed = {
                onEvent(FileBrowserEvent.DeleteSelected)
                deleteDialogVisible.value = false
            }
        )
    }

    if (!state.isKeyLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = "First, need to load key")
                Image(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                    contentDescription = null
                )
            }
            val transition = rememberInfiniteTransition(label = "inf").animateFloat(
                initialValue = 0f,
                targetValue = 42f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrow_pointer"
            )
            Image(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_arrow_back_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(
                        bottom = scaffoldPaddingValues.calculateBottomPadding() + 18.dp,
                        end = 62.dp
                    )
                    .align(Alignment.BottomEnd)
                    .rotate(-90f)
                    .graphicsLayer {
                        translationX = transition.value
                    }
            )
        }
    } else {
        BrowserBlock<FileBrowserEvent>(
            files = state.files,
            currentFolderName = state.currentFolderName,
            paddingBottom = scaffoldPaddingValues.calculateBottomPadding(),
            isSelectionEnabled = true,
            onEvent = { onEvent(it) },
            actions = buildActions(state, onEvent, deleteDialogVisible),
            isPageEmpty = state.isPageEmpty,
            isInRoot = state.isInRoot,
            showLoading = true
        )
    }

    val bsState = confirmBsExpanded.value
    if (bsState.shouldShow())
        FilesLoadInitialization(bsState.getArgs()) {
            confirmBsExpanded.value = BottomSheetPolicy.Collapsed
        }
}

@OptIn(ExperimentalFoundationApi::class)
private fun buildActions(
    state: FileBrowserUiState,
    onEvent: (FileBrowserEvent) -> Unit,
    deleteDialogVisible: MutableState<Boolean>,
): Set<@Composable (RowScope) -> Unit> = buildSet {
    if (state.hasSelected) {
        add {
            BadgedBox(
                badge = {
                    Text(
                        text = state.selectedCount.toString(),
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 8.dp),
                        Color.Red,
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_post_add_24),
                    contentDescription = null,
                    modifier = Modifier
                        .combinedClickable(
                            interactionSource = remember { mutableStateOf(MutableInteractionSource()) }.value,
                            indication = ripple(),
                            onLongClick = {
                                onEvent(FileBrowserEvent.RemoveSelection)
                            },
                            onClick = {
                                onEvent(FileBrowserEvent.AddSelection)
                            }
                        )
                        .size(26.dp),
                    tint = Color.Black
                )
            }

        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
        add {
            Icon(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_delete_outline_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        deleteDialogVisible.value = true
                    }
                    .size(26.dp),
                tint = Color.Black
            )
        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
    }
    if (state.sourceState.isMsdAttached) {
        add {
            Icon(
                painter = painterResource(
                    id = if (state.sourceState.currentSource == Source.INTERNAL)
                        ru.barinov.core.R.drawable.baseline_sd_storage_24
                    else ru.barinov.core.R.drawable.mass_storage_device
                ),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onEvent(SourceChanged)
                    }
                    .size(26.dp),
                tint = if (state.sourceState.currentSource == Source.INTERNAL) Color.Black else LocalContentColor.current
            )
        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
    }
    if (!state.isPageEmpty) {
        add {
            val sortDropDownExpanded = remember { mutableStateOf(false) }
            Box {
                Icon(
                    painter = painterResource(id = ru.barinov.core.R.drawable.outline_sort_24),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { sortDropDownExpanded.value = true }
                        .size(26.dp),
                    tint = Color.Black
                )
                SortDropDownMenu(
                    isExpanded = sortDropDownExpanded.value,
                    selectedSort = state.selectedSortType,
                    onDismissRequest = { sortDropDownExpanded.value = false },
                    onEvent = { onEvent(it) }
                )
            }
        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
    }
}
