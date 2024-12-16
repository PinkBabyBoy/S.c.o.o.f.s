package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.barinov.core.ui.getActivity
import ru.barinov.core.ui.mainGreen

@Composable
fun FileBrowserHomeScreen(mainController: NavController) {
    val context = LocalContext.current
    val localNavController = rememberNavController()

    BackHandler {
        context.getActivity()?.finish()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomBarVisibility = remember { mutableStateOf(true) }


    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = mainGreen,
                    contentColor = Color.White,
                ){
                    Row {
                        Icon(painter = painterResource(id = ru.barinov.core.R.drawable.info), contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = it.visuals.message)
                    }

                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisibility.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) { BrowserBottomNavBar(navController = localNavController) }

        },
    ) {
        Column (modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars).fillMaxWidth().background(Color.White))
            FileBrowserNavHost(
                navController = localNavController,
                startDestination = FileBrowserRout.FILE_OBSERVER.name,
                scaffoldPaddings = it,
                snackbarHostState = snackbarHostState,
                bottomNavBarVisibility = { bottomBarVisibility.value = it }
            )
        }
            AnimatedVisibility(
                visible = bottomBarVisibility.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) { ProtectNavigationBar() }

    }
}