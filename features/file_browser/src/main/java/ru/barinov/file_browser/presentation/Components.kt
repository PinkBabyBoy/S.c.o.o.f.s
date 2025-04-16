package ru.barinov.file_browser.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.barinov.core.SortType
import ru.barinov.core.topBarHeader
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.models.Sort
import ru.barinov.core.ui.mainGreen
import ru.barinov.file_browser.events.DeleteSelected
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.states.AppbarState


private val sortTypes = listOf(
    Sort(
        ru.barinov.core.R.string.sort_new_first,
        SortType.NEW_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_old_first,
        SortType.OLD_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_big_first,
        SortType.BIG_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_small_first,
        SortType.SMALL_FIRST
    ),
    Sort(
        ru.barinov.core.R.string.sort_default,
        SortType.AS_IS
    )
)

internal typealias Action = @Composable RowScope.() -> Unit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserAppBar(
    titleString: String,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onNavigateUpClicked: () -> Unit = {},
    onEvent: (FieObserverEvent) -> Unit,
    showArrow: Boolean,
    appbarState: AppbarState

) {
    val deleteDialogVisible = remember { mutableStateOf(false) }
    if (deleteDialogVisible.value) {
        ScoofAlertDialog(
            title = "Delete selected?",
            message = "All selected files will be removed",
            onDismissRequest = { deleteDialogVisible.value = false },
            onConfirmed = {
                onEvent(DeleteSelected)
                deleteDialogVisible.value = false
            }
        )
    }
    val title =
        @Composable {
            if(titleString.isNotEmpty()) {
                Text(
                    text = titleString,
                    modifier = Modifier.padding(start = 16.dp),
                    style = topBarHeader()
                )
            }
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
    Box {
        val spotLightOffsetState = remember { mutableStateOf<Offset?>(null) }
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
                when (appbarState) {
                    is AppbarState.Browser -> fileBrowserActions(appbarState, onEvent,  spotLightOffsetState)
                    is AppbarState.Containers -> containersActions(appbarState, onEvent, spotLightOffsetState)
                    is AppbarState.KeySelection -> keySelectorActions(appbarState, onEvent,  spotLightOffsetState)
                    AppbarState.None -> emptySet()
                }.forEach { action ->
                    action()
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
        val spotLightOffset = spotLightOffsetState.value
        AnimatedVisibility(spotLightOffset != null) {
            val cornerRadiusPx = with(LocalDensity.current) { 18.dp.toPx() }
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                spotLightOffset?.also { offset ->
                    val path = Path().apply {
                        addOval(
                            Rect(
                                center = offset.copy(
                                    x = offset.x + 14.dp.toPx(),
                                    y = offset.y - 38.dp.toPx()
                                ),
                                radius = cornerRadiusPx
                            )
                        )
                    }
                    clipPath(path, clipOp = ClipOp.Difference) {
                        drawRect(Color.Black.copy(alpha = 0.75f))
                    }

                } ?: drawRect(Color.Black.copy(alpha = 0.75f))
            }
        }
    }
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
    selectedSort: SortType,
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


