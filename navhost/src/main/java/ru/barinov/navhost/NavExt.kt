package ru.barinov.navhost

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlider(
    fromScreen: String?,
    openScreen: String
): EnterTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == BottomBarRout.FILE_OBSERVER.name -> AnimatedContentTransitionScope.SlideDirection.Right
        openScreen == BottomBarRout.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> AnimatedContentTransitionScope.SlideDirection.Right
    }
    return slideIntoContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitSlider(
    fromScreen: String?,
    openScreen: String
): ExitTransition? {
    if (fromScreen == null) return null
    val slideDirection = when {
        openScreen == BottomBarRout.FILE_OBSERVER.name && fromScreen != BottomBarRout.FILE_OBSERVER.name
            -> AnimatedContentTransitionScope.SlideDirection.Right

        openScreen == BottomBarRout.SETTINGS.name -> AnimatedContentTransitionScope.SlideDirection.Left

        else -> AnimatedContentTransitionScope.SlideDirection.Left
    }
    return slideOutOfContainer(
        slideDirection,
        animationSpec = tween(250)
    )
}