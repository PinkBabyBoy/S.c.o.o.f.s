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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.barinov.file_browser.R
import ru.barinov.file_browser.events.ImageDetailsEvent
import ru.barinov.file_browser.states.ImageFileScreenUiState
import ru.barinov.ui_ext.RegisterLifecycleCallbacks
import ru.barinov.ui_ext.mainGreen

@Composable
fun ImageFileScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    state: ImageFileScreenUiState,
    onEvent: (ImageDetailsEvent) -> Unit,
    bottomNavBarVisibility: (Boolean) -> Unit,
) {
    bottomNavBarVisibility(false)
    RegisterLifecycleCallbacks(
        onDispose = {bottomNavBarVisibility(true)}
    )
    Box(Modifier.background(Color.Black).fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
        if (state.bitmapToShow == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = mainGreen)
        } else {
            Image(
                state.bitmapToShow.asImageBitmap(), null, modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
            Row(Modifier.align(alignment = Alignment.BottomStart).fillMaxWidth().padding(bottom = 64.dp), horizontalArrangement = Arrangement.Center) {
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

}