package ru.barinov.file_browser.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.barinov.file_browser.R
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.ui_ext.bottomNavGreen

private val fileBrowserTopLevelScreens = setOf(
    TopLevelScreen(
        FileBrowserRout.CONTAINERS,
        ru.barinov.ui_ext.R.string.containers_label,
        ru.barinov.core.R.drawable.baseline_storage_24
    ),
    TopLevelScreen(
        FileBrowserRout.FILE_PICKER,
        ru.barinov.ui_ext.R.string.files_label,
        ru.barinov.core.R.drawable.baseline_sd_storage_24
    ),
    TopLevelScreen(
        FileBrowserRout.KEY_PICKER,
        ru.barinov.ui_ext.R.string.key_label,
        ru.barinov.core.R.drawable.baseline_key_24
    )
)

private val sortTypes = listOf(
    Sort(
        ru.barinov.ui_ext.R.string.sort_new_first,
        Sort.Type.NEW_FIRST
    ),
    Sort(
        ru.barinov.ui_ext.R.string.sort_old_first,
        Sort.Type.OLD_FIRST
    ),
    Sort(
        ru.barinov.ui_ext.R.string.sort_big_first,
        Sort.Type.BIG_FIRST
    ),
    Sort(
        ru.barinov.ui_ext.R.string.sort_small_first,
        Sort.Type.SMALL_FIRST
    ),
    Sort(
        ru.barinov.ui_ext.R.string.sort_default,
        Sort.Type.AS_IS
    )
)

@Composable
fun BrowserBottomNavBar(
    navController: NavController
) {
    val currentEntry = navController.currentBackStackEntryAsState().value
    NavigationBar(
        containerColor = bottomNavGreen,
        modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserAppBar(
    folderName: String,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onNavigateUpClicked: () -> Unit,
    showArrow: Boolean,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet()
) {
    val title =
        @Composable { Text(text = folderName, Modifier.padding(start = 16.dp), fontSize = 14.sp) }
    val navigationIcon = @Composable {
        AnimatedVisibility(showArrow, enter = scaleIn(), exit = scaleOut()) {
            Icon(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_arrow_back_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onNavigateUpClicked() }
                    .padding(start = 12.dp)
            )
        }
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
        },
        modifier = Modifier.drawBehind {
            drawLine(
                color = Color.Gray,
                start = Offset(32f, size.height),
                end = Offset(size.width - 32f, size.height),
                strokeWidth = 0.5.dp.toPx()
            )
        }
    )
}

private fun NavBackStackEntry?.isSelected(itemRout: FileBrowserRout): Boolean =
    this?.destination?.hierarchy?.any { it.route == itemRout.name } ?: false


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

@Composable
fun SortDropDownMenu(
    isExpanded: Boolean,
    selectedSort: Sort.Type,
    onDismissRequest: () -> Unit,
    onEvent: (FileBrowserEvent) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { onDismissRequest() }
    ) {
        sortTypes.forEach {
            DropdownMenuItem(
                trailingIcon = {
                    RadioButton(
                        selected = it.type == selectedSort,
                        onClick = {
                            onDismissRequest()
                            onEvent(FileBrowserEvent.SortSelected(it.type))
                        }
                    )
                },
                text = {
                    Text(text = stringResource(id = it.text))
                },
                onClick = {
                    onDismissRequest()
                    onEvent(FileBrowserEvent.SortSelected(it.type))
                }
            )
        }
    }
}
