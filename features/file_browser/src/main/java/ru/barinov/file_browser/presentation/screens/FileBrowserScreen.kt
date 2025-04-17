package ru.barinov.file_browser.presentation.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.file_browser.NoArgsRouts
import ru.barinov.file_browser.events.DeleteSelected
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.presentation.BrowserBlock
import ru.barinov.file_browser.presentation.Pages
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.sideEffects.OpenImageFile
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.file_browser.toImageDetails

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
    val isPageOnScreen = remember {
        derivedStateOf {
            pageState.intValue == Pages.FILE_BROWSER.ordinal
        }
    }
    if(!isPageOnScreen.value) return
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
                navController.navigate(NoArgsRouts.ENCRYPTION_START_BOTTOM_SHEET.name)
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
                    modifier = Modifier
                        .clickable { openPage(Pages.KEY_PICKER.ordinal) }
                        .padding(top = 24.dp)
                        .size(transitionSize.value.toInt().dp)
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
            isPageEmpty = state.isPageEmpty,
            isInRoot = state.isInRoot,
            showLoading = true,
            appbarState = state.appBarState,
            hasSelected = state.hasSelected
        )
    }
}



