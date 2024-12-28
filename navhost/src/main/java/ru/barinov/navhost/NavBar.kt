package ru.barinov.navhost

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.barinov.core.navigation.Routes
import ru.barinov.core.ui.mainGreen

private val topLevelScreens = setOf(
    TopLevelScreen(
        Routes.BROWSER,
        ru.barinov.core.R.string.containers_label,
        ru.barinov.core.R.drawable.baseline_storage_24
    ),
    TopLevelScreen(
        Routes.SETTINGS,
        ru.barinov.core.R.string.settings_label,
        ru.barinov.core.R.drawable.baseline_settings_24
    ),
)

@Composable
fun BrowserBottomNavBar(
    navController: NavController
) {
    val currentEntry = navController.currentBackStackEntryAsState().value
    NavigationBar(
        containerColor = mainGreen,
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only( WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .padding(horizontal = 12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp, 18.dp, 0.dp, 0.dp))
    ) {
        topLevelScreens.forEach { destination ->
            val selected = currentEntry.isSelected(destination.rout)
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors().copy(
                    selectedTextColor = Color(0xFFF0EFEF),
                    selectedIndicatorColor = Color(0xFFF0EFEF)
                ),
                alwaysShowLabel = selected,
                selected = selected,
                icon = { NavigationIcon(destination.iconImgDrawable, selected) },
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

@Composable
private fun NavigationItemLabel(destination: TopLevelScreen) {
    Text(text = stringResource(id = destination.label), fontSize = 10.sp)
}

@Composable
private fun NavigationIcon(@DrawableRes resId: Int, selected: Boolean) {
    Icon(
        painter = painterResource(id = resId),
        contentDescription = null,
        Modifier.size(18.dp),
        tint = if (!selected) Color(0xFF525252) else LocalContentColor.current
    )
}

private fun NavBackStackEntry?.isSelected(itemRout: Routes): Boolean =
    this?.destination?.hierarchy?.any { it.route == itemRout.name } ?: false