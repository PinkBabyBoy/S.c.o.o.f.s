package ru.barinov.file_browser.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

private val fileBrowserTopLevelScreens = setOf(
    TopLevelScreen(FileBrowserRout.CONTAINERS, "KOnt", ru.barinov.core.R.drawable.baseline_storage_24),
    TopLevelScreen(FileBrowserRout.FILE_PICKER, "File", ru.barinov.core.R.drawable.baseline_sd_storage_24),
    TopLevelScreen(FileBrowserRout.KEY_PICKER, "Key", ru.barinov.core.R.drawable.baseline_key_24)
)

@Composable
fun BrowserBottomNavBar(
    navController: NavController
) {
    val currentEntry = navController.currentBackStackEntryAsState().value
    NavigationBar(
        Modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .padding(horizontal = 12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp, 18.dp, 0.dp, 0.dp))
    ) {
        fileBrowserTopLevelScreens.forEach { destination ->
            val selected = currentEntry.isSelected(destination.rout)
            NavigationBarItem(
                alwaysShowLabel = selected,
                selected = selected,
                icon = { NavigationIcon(destination.iconImgDrawable) },
                label = { NavigationItemLabel(destination) },
                onClick = {
                    if (!selected) {
                        navController.navigate(destination.rout.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserAppBar(
    folderName: String,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onNavigateUpClicked: () -> Unit,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet()
) {
    val title = @Composable { Text(text = folderName, Modifier.padding(start = 16.dp), fontSize = 14.sp) }
    val navigationIcon = @Composable {
        Icon(
            painter = painterResource(id = ru.barinov.core.R.drawable.baseline_arrow_back_24),
            contentDescription = null,
            modifier = Modifier
                .clickable { onNavigateUpClicked() }
                .padding(start = 12.dp)
        )
    }
    TopAppBar(
        title = { title() },
        navigationIcon = { navigationIcon() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        windowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp
        ),
        scrollBehavior = topAppBarScrollBehavior,
        actions = {
            actions.forEach { action ->
                action(this)
            }
        }
    )
}

private fun NavBackStackEntry?.isSelected(itemRout: FileBrowserRout): Boolean =
    this?.destination?.hierarchy?.any { it.route == itemRout.name } ?: false


@Composable
private fun NavigationItemLabel(destination: TopLevelScreen) {
    Text(text = destination.label, fontSize = 10.sp)
}

@Composable
private fun NavigationIcon(@DrawableRes resId: Int) {
    Image(painter = painterResource(id = resId), contentDescription = null, Modifier.size(16.dp))
}