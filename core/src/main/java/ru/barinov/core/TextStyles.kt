package ru.barinov.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp


fun topBarHeader() = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)


fun headerDefault() = TextStyle(
    fontSize = 18.sp,
    lineHeight = 24.sp,
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)

val headerBig = TextStyle(
    fontSize = 24.sp,
    lineHeight = 24.sp,
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)

