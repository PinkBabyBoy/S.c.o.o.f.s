package ru.barinov.file_browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.barinov.ui_ext.getActivity


private val fileBrowserTopLevelScreens = setOf(
    TopLevelScreen(FileBrowserRout.CONTAINERS, "KOnt", ru.barinov.core.R.drawable.baseline_arrow_back_24),
    TopLevelScreen(FileBrowserRout.FILE_PICKER, "File", ru.barinov.core.R.drawable.baseline_arrow_back_24),
    TopLevelScreen(FileBrowserRout.KEY_PICKER, "Key", ru.barinov.core.R.drawable.baseline_arrow_back_24)
)

@Composable
fun FileBrowserHomeScreen(mainController: NavController) {
    val context = LocalContext.current
    val localNavController = rememberNavController()

    BackHandler {
        context.getActivity()?.finish()
    }

    Scaffold(
        bottomBar = {
            BrowserBottomNavBar(
                screens = fileBrowserTopLevelScreens,
                navController = localNavController
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(top = it.calculateTopPadding())
        ) {
            FileBrowserNavHost(localNavController, FileBrowserRout.CONTAINERS.name)
        }

    }
}