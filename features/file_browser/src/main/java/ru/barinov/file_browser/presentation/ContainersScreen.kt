package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.barinov.file_browser.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Containers() {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Containers") },
            navigationIcon = { Icon(painter = painterResource(id = ru.barinov.core.R.drawable.baseline_arrow_back_24), contentDescription = null) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            windowInsets = WindowInsets(
                top = 0.dp,
                bottom = 0.dp
            ),
//            scrollBehavior = topAppBarScrollBehavior,
            actions = {
//                actions.forEach { action ->
//                    action(this)
//                }
            }
        )
    }
}
