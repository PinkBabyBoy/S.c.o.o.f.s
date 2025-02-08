package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

@Composable
fun ContainerContent(
    uiState: State<Int>
) {
    Box(Modifier.fillMaxSize()){
        Text( uiState.value.toString(), modifier = Modifier.align(Alignment.Center))
    }
}