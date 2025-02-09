package ru.barinov.file_browser.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ru.barinov.file_browser.events.ImageDetailsEvent
import ru.barinov.file_browser.sideEffects.ImageFileDetailsSideEffects
import ru.barinov.file_browser.states.ImageFileScreenUiState
import ru.barinov.file_browser.viewModels.InitializationParams
import ru.barinov.core.ui.BottomSheetPolicy
import ru.barinov.core.ui.RegisterLifecycleCallbacks
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.getArgs
import ru.barinov.core.ui.mainGreen
import ru.barinov.core.ui.shouldShow

@Composable
fun ImageFileScreen(
    paddingValues: PaddingValues,
    sideEffects: Flow<ImageFileDetailsSideEffects>,
    navController: NavController,
    state: ImageFileScreenUiState,
    onEvent: (ImageDetailsEvent) -> Unit,
    bottomNavBarVisibility: (Boolean) -> Unit,
) {
    bottomNavBarVisibility(false)
    val confirmBsExpanded =
        remember { mutableStateOf<BottomSheetPolicy>(BottomSheetPolicy.Collapsed) }
    RegisterLifecycleCallbacks(
        onDispose = { bottomNavBarVisibility(true) }
    )
    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            is ImageFileDetailsSideEffects.ShowAddFilesDialog ->
                confirmBsExpanded.value = BottomSheetPolicy.Expanded(InitializationParams.Direct(sideEffect.file))
        }
    }
    Box(
        Modifier
            .background(Color.Black)
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        if (state.bitmapToShow == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = mainGreen)
        } else {
            Image(
                state.bitmapToShow.asImageBitmap(), null, modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_post_add_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 32.dp)
                    .clickable { onEvent(ImageDetailsEvent.SaveToContainer) }
            )
            Row(
                Modifier
                    .align(alignment = Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 64.dp), horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = ru.barinov.core.R.drawable.outline_rotate_left_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.clickable { onEvent(ImageDetailsEvent.RotateLeft) }
                )
                Spacer(modifier = Modifier.width(64.dp))
                Icon(
                    painter = painterResource(id = ru.barinov.core.R.drawable.outline_rotate_right_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.clickable { onEvent(ImageDetailsEvent.RotateRight) }
                )
            }
        }
    }
    if(confirmBsExpanded.value.shouldShow()) {
        FilesLoadInitialization(confirmBsExpanded.value.getArgs()) { confirmBsExpanded.value = BottomSheetPolicy.Collapsed }
    }

}