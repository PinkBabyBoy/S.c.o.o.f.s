package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.barinov.core.R
import ru.barinov.ui_ext.ColorPair
import ru.barinov.ui_ext.DecorStyle
import ru.barinov.ui_ext.getActivity
import ru.barinov.ui_ext.mainGreen

@Composable
fun FileBrowserHomeScreen(mainController: NavController) {
    val context = LocalContext.current
    val localNavController = rememberNavController()

    BackHandler {
        context.getActivity()?.finish()
    }
    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = mainGreen,
                    contentColor = Color.White
                )
            }
        },
        bottomBar = { BrowserBottomNavBar(navController = localNavController) },
    ) {
        DecorStyle(
            ColorPair((0xFFFCFFFD).toInt(), (0xFFFCFFFD).toInt()),
            ColorPair((0xFF65C08F).toInt(), (0xFF65C08F).toInt()),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            FileBrowserNavHost(
                localNavController,
                FileBrowserRout.CONTAINERS.name,
                it,
                snackbarHostState
            )
        }

    }
}