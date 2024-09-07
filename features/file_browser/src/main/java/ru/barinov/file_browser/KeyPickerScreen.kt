package ru.barinov.file_browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun KeyPicker() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Red)) {
        Text(text = "Screen 3")
    }
}