package ru.barinov.navhost

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import ru.barinov.core.navigation.Routes

fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlider(
    fromScreen: String?,
    openScreen: String
): EnterTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == Routes.BROWSER.name -> AnimatedContentTransitionScope.SlideDirection.Right
        openScreen == Routes.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> error("Wrong direction")
    }
    return slideIntoContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.exitSlider(
    fromScreen: String?,
    openScreen: String
): ExitTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == Routes.BROWSER.name && fromScreen != Routes.BROWSER.name
            -> AnimatedContentTransitionScope.SlideDirection.Right

        openScreen == Routes.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> error("Wrong direction")
    }
    return slideOutOfContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}