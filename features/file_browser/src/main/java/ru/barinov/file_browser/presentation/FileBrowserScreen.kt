package ru.barinov.file_browser.presentation

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.internal.enableLiveLiterals
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.Source
import ru.barinov.file_browser.R

import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.ui_ext.BottomSheetPolicy
import ru.barinov.ui_ext.SingleEventEffect
import ru.barinov.ui_ext.getActivity

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
    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> navController.navigateUp()
            is ShowInfo -> TODO()
        }
    }

    if (deleteDialogVisible.value) {
        DeleteSelectedAlertDialog(
            dismiss = { deleteDialogVisible.value = false },
            onConfirmed = {
                onEvent(FileBrowserEvent.Delete)
                deleteDialogVisible.value = false
            }
        )
    }

    if (!state.isKeyLoaded) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "First, need to load key",
                )
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
            isInRoot = state.isInRoot
        )
    }

//    ((confirmBsExpanded.value as? BottomSheetPolicy.Expanded<*>)?.args as? ConfirmBottomSheetArgs)?.let {
//
//    }
}

private fun buildActions(
    state: FileBrowserUiState,
    onEvent: (FileBrowserEvent) -> Unit,
    deleteDialogVisible: MutableState<Boolean>
): Set<@Composable (RowScope) -> Unit> = buildSet {
    if (state.hasSelected) {
        add {
            Image(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_post_add_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onEvent(FileBrowserEvent.AddSelection)
                    }
                    .size(42.dp)
                    .padding(end = 16.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
        add {
            Image(
                painter = painterResource(id = ru.barinov.ui_ext.R.drawable.baseline_delete_outline_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        deleteDialogVisible.value = true
                    }
                    .size(42.dp)
                    .padding(end = 16.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
    if (state.sourceState.isMsdAttached) {
        add {
            Image(
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
                    .size(32.dp)
                    .padding(end = 16.dp),
                colorFilter = if (state.sourceState.currentSource == Source.INTERNAL)
                    ColorFilter.tint(Color.Black)
                else null
            )
        }
    }
}

@Composable
fun DeleteSelectedAlertDialog(dismiss: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = "Delete selected?")
        },
        text = {
            Text(text = "All selected files will be removed")
        },
        onDismissRequest = { dismiss() },
        dismissButton = {
            Button(onClick = dismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}
