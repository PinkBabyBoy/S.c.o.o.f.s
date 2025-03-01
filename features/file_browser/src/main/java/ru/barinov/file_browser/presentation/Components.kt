package ru.barinov.file_browser.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.barinov.core.SortType
import ru.barinov.core.topBarHeader
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.models.Sort
import ru.barinov.core.ui.mainGreen


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


