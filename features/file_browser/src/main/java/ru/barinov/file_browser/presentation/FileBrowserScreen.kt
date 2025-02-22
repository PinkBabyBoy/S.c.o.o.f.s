package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateSizeAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import ru.barinov.file_browser.viewModels.InitializationParams
import ru.barinov.core.ui.BottomSheetPolicy
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.getArgs
import ru.barinov.core.ui.shouldShow
import ru.barinov.file_browser.events.DeleteSelected
import ru.barinov.file_browser.events.OnboardingFinished
import ru.barinov.file_browser.events.RemoveSelection
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.sideEffects.OpenImageFile
import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.orEmpty

@Composable
fun FileBrowserScreen(
    state: FileBrowserUiState,
    scaffoldPaddingValues: PaddingValues,
    sideEffects: Flow<FileBrowserSideEffect>,
    navController: NavController,
    onEvent: (FileBrowserEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    openPage: (Int) -> Unit,
    pageState: MutableIntState
) {
    val confirmBsExpanded =
        remember { mutableStateOf<BottomSheetPolicy>(BottomSheetPolicy.Collapsed) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localCoroutine = rememberCoroutineScope()
    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> openPage(Pages.CONTAINERS.ordinal)
            is ShowInfo -> localCoroutine.launch {
                snackbarHostState.showSnackbar(
                    context.getString(
                        sideEffect.text
                    ), withDismissAction = true
                )
            }

            is OpenImageFile
                -> navController.navigate(toImageDetails(sideEffect.fileId))

            is FileBrowserSideEffect.ShowAddFilesDialog -> {
                confirmBsExpanded.value =
                    BottomSheetPolicy.Expanded(InitializationParams.Selected(sideEffect.selectedFiles))
            }
        }
    }

    if (deleteDialogVisible.value) {
        ScoofAlertDialog(
            title = "Delete selected?",
            message = "All selected files will be removed",
            onDismissRequest = { deleteDialogVisible.value = false },
            onConfirmed = {
                onEvent(DeleteSelected)
                deleteDialogVisible.value = false
            }
        )
    }

    if (!state.isKeyLoaded) {
        BackHandler {
            openPage(Pages.CONTAINERS.ordinal)
        }
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
                val transitionSize = rememberInfiniteTransition(label = "infSize").animateFloat(
                    initialValue = 32f,
                    targetValue = 54f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutLinearInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "arrow_pointer"
                )
                Image(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                    contentDescription = null,
                    modifier = Modifier.clickable { openPage(Pages.KEY_PICKER.ordinal) }.padding(top = 24.dp).size(transitionSize.value.toInt().dp)
                )
            }
//            Image(
//                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_arrow_back_24),
//                contentDescription = null,
//                modifier = Modifier
//                    .padding(
//                        bottom = scaffoldPaddingValues.calculateBottomPadding() - 24.dp,
//                        end = 134.dp
//                    )
//                    .align(Alignment.BottomEnd)
//                    .rotate(-90f)
//                    .graphicsLayer {
//                        translationX = transition.value
//                    }
//            )
        }
    } else {
        BrowserBlock<FileBrowserEvent, FileUiModel>(
            files = state.files,
            currentFolderName = state.currentFolderName,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun buildActions(
    state: FileBrowserUiState,
    onEvent: (FileBrowserEvent) -> Unit,
    deleteDialogVisible: MutableState<Boolean>,
): Set<@Composable (RowScope) -> Unit> = buildSet {
    if (state.hasSelected) {
        add {
            val onbData = state.fileBrowserOnboarding[OnBoarding.ADD_SELECTED].orEmpty()
            OnBoarding(
                title = stringResource(ru.barinov.core.R.string.key_creation_title_ond),
                state = onbData,
                tooltipText = stringResource(ru.barinov.core.R.string.key_creation_message_ond),
                onClick = { onEvent(OnboardingFinished(OnBoarding.ADD_SELECTED)) },
                width = 42.dp,
                hasNext = false
            ) {
                BadgedBox(
                    badge = {
                        Text(
                            text = state.selectedCount.toString(),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(start = 8.dp),
                            color = Color.Red,
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = ru.barinov.core.R.drawable.baseline_post_add_24),
                        contentDescription = null,
                        modifier = Modifier
                            .combinedClickable(
                                interactionSource = remember {
                                    mutableStateOf(MutableInteractionSource())
                                }.value,
                                indication = ripple(),
                                onLongClick = {
                                    onEvent(RemoveSelection)
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
            val onbData = state.fileBrowserOnboarding[OnBoarding.CHANGE_SOURCE].orEmpty()
            OnBoarding(
                title = stringResource(ru.barinov.core.R.string.key_creation_title_ond),
                state = onbData,
                tooltipText = stringResource(ru.barinov.core.R.string.key_creation_message_ond),
                onClick = { onEvent(OnboardingFinished(OnBoarding.CHANGE_SOURCE)) },
                width = 42.dp,
                hasNext = false
            ) {
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
        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
    }
    if (!state.isPageEmpty) {
        add {
            val onbData = state.fileBrowserOnboarding[OnBoarding.SORT_FILES].orEmpty()
            OnBoarding(
                title = stringResource(ru.barinov.core.R.string.key_creation_title_ond),
                state = onbData,
                tooltipText = stringResource(ru.barinov.core.R.string.key_creation_message_ond),
                onClick = { onEvent(OnboardingFinished(OnBoarding.SORT_FILES)) },
                width = 42.dp,
                hasNext = false
            ) {
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
        }
        add { Spacer(modifier = Modifier.width(16.dp)) }
    }
}
