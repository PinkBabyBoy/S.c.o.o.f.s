package ru.barinov.core

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.expandedWithOffset(offsetDp: Dp): Modifier {
    return this then Modifier.height(getScreenHeightWithLOffset(offsetDp.value.toInt()).dp)
}