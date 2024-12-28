package ru.barinov.navhost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import ru.barinov.core.ui.mainGreen

@Composable
fun ProtectNavigationBar(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val tappableElement = WindowInsets.tappableElement
    val navigationBars = WindowInsets.navigationBars
    val bottomPixels = tappableElement.getBottom(density)
    val usingTappableBars = remember(bottomPixels) { bottomPixels != 0 }
    val barHeight = remember(bottomPixels) {
        if(usingTappableBars)
            tappableElement.asPaddingValues(density).calculateBottomPadding()
        else navigationBars.asPaddingValues(density).calculateBottomPadding()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(
            modifier = Modifier
                .background(mainGreen)
                .fillMaxWidth()
                .height(barHeight)
        )
    }
}