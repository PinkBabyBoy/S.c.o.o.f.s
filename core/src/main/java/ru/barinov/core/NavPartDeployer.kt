package ru.barinov.core

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

interface NavPartDeployer {

    fun NavGraphBuilder.deploy(
        navController: NavController,
        modifier: Modifier = Modifier
    )
}
