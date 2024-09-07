package ru.barinov.file_browser

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BrowserBottomNavBar(
    screens: Set<TopLevelScreen>,
    navController: NavController
) {
    val rememberCurrentTopLevelDestination = remember { mutableStateOf(FileBrowserRout.CONTAINERS) }
    val currentEntry = navController.currentBackStackEntryAsState().value
    NavigationBar(
        Modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .padding(horizontal = 12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(22.dp, 22.dp, 0.dp, 0.dp))
    ) {
        screens.forEach { destination ->
            val selected = currentEntry.isSelected(destination.rout)
            NavigationBarItem(
                alwaysShowLabel = selected,
                selected = selected,
                icon = { NavigationIcon(destination.iconImgDrawable) },
                label = { NavigationItemLabel(destination) },
                onClick = {
                    if (!selected) {
                        navController.navigate(destination.rout.name){
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                        rememberCurrentTopLevelDestination.value = destination.rout
                    }
                }
            )
        }

    }
}

private fun NavBackStackEntry?.isSelected(itemRout: FileBrowserRout): Boolean =
    this?.destination?.hierarchy?.any { it.route == itemRout.name } ?:false


@Composable
private fun NavigationItemLabel(destination: TopLevelScreen) {
    Text(text = destination.label, fontSize = 10.sp)
}

@Composable
private fun NavigationIcon(@DrawableRes resId: Int) {
    Image(painter = painterResource(id = resId), contentDescription = null, Modifier.size(16.dp))
}