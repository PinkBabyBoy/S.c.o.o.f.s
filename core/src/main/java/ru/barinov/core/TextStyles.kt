package ru.barinov.core

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

val topBarHeaderStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
)