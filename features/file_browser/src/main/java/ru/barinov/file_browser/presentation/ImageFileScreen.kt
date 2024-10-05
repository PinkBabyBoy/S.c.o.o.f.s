package ru.barinov.file_browser.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavController
import ru.barinov.file_browser.states.ImageFileScreenUiState
import ru.barinov.ui_ext.mainGreen

@Composable
fun ImageFileScreen(
    navController: NavController,
    state: ImageFileScreenUiState
) {
    Box {
        if (state.bitmapToShow == null) {
            CircularProgressIndicator(Modifier.fillMaxSize().align(Alignment.Center), color = mainGreen)
        } else {
            Image(state.bitmapToShow.asImageBitmap(), null, modifier = Modifier.fillMaxSize().align(Alignment.Center))
        }
    }

}