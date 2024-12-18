package ru.barinov.file_browser.presentation

import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import me.jahnen.libaums.core.BuildConfig
import ru.barinov.core.topBarHeader
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.models.Sort
import ru.barinov.file_browser.models.TopLevelScreen
import ru.barinov.core.ui.mainGreen
import ru.barinov.onboarding.OnBoarding

private val fileBrowserTopLevelScreens = setOf(
    TopLevelScreen(
        FileBrowserRout.FILE_OBSERVER,
        ru.barinov.core.R.string.containers_label,
        ru.barinov.core.R.drawable.baseline_storage_24
    ),
    TopLevelScreen(
        FileBrowserRout.SETTINGS,
        ru.barinov.core.R.string.files_label,
        ru.barinov.core.R.drawable.baseline_sd_storage_24
    ),
)

private val sortTypes = listOf(
    Sort(
        ru.barinov.core.R.string.sort_new_first,
        Sort.Type.NEW_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_old_first,
        Sort.Type.OLD_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_big_first,
        Sort.Type.BIG_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_small_first,
        Sort.Type.SMALL_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_default,
        Sort.Type.AS_IS
    )
)

@Composable
fun BrowserBottomNavBar(
    navController: NavController
) {
//    val sides = if(VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM)
//        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
//    else  WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
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
    titleString: String,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onNavigateUpClicked: () -> Unit = {},
    showArrow: Boolean,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet(),
) {
    val title =
        @Composable {
            Text(
                text = titleString,
                modifier = Modifier.padding(start = 16.dp),
                style = topBarHeader()
            )
        }
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
        modifier = Modifier.background(Color.White),
        expanded = isExpanded,
        onDismissRequest = { onDismissRequest() }
    ) {
        sortTypes.forEach {
            DropdownMenuItem(
                trailingIcon = {
                    RadioButton(
                        colors = RadioButtonDefaults.colors().copy(selectedColor = mainGreen),
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

@Composable
fun ProtectNavigationBar(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val tappableElement = WindowInsets.tappableElement
    val navigationBars = WindowInsets.navigationBars
    val bottomPixels = tappableElement.getBottom(density)
    val usingTappableBars = remember(bottomPixels) { bottomPixels != 0 }
    val barHeight = remember(bottomPixels) {
        if(usingTappableBars)
        tappableElement.asPaddingValues(density).calculateBottomPadding()
        else navigationBars.asPaddingValues(density).calculateBottomPadding()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(
                modifier = Modifier
                    .background(mainGreen)
                    .fillMaxWidth()
                    .height(barHeight)
            )
        }
}
