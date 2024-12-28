package ru.barinov.navhost

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.barinov.core.navigation.Routes

@Immutable
class TopLevelScreen(
    val rout: Routes,
    @StringRes val label: Int,
    @DrawableRes val iconImgDrawable: Int
)

