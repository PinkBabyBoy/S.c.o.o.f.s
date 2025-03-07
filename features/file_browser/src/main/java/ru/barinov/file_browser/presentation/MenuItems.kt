package ru.barinov.file_browser.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.barinov.core.Source
import ru.barinov.file_browser.events.DeleteSelected
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.events.OnboardingFinished
import ru.barinov.file_browser.events.RemoveSelection
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.states.AppbarState
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.Tooltip

@Composable
fun keySelectorSet(
    appbarState: AppbarState.KeySelection,
    onEvent: (KeySelectorEvent) -> Unit,
    spotLightOffsetState: MutableState<Offset?>
): Set<Action> = buildSet {
    add {
        AnimatedVisibility(appbarState.sourceState.isMsdAttached) {
            val (currentOnboarding) = appbarState.keySelectionOnboarding
            val expandedState =
                remember { mutableStateOf(currentOnboarding == OnBoarding.CHANGE_SOURCE) }
            Box {
                Icon(
                    painter = painterResource(
                        id = if (appbarState.sourceState.currentSource == Source.INTERNAL)
                            ru.barinov.core.R.drawable.baseline_sd_storage_24
                        else ru.barinov.core.R.drawable.mass_storage_device
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            onEvent(SourceChanged)
                        }
                        .size(26.dp),
                    tint = if (appbarState.sourceState.currentSource == Source.INTERNAL) Color.Black else LocalContentColor.current
                )
                androidx.compose.animation.AnimatedVisibility(expandedState.value) {
                    Tooltip(
                        onboardingInfo = appbarState.keySelectionOnboarding,
                        onDismiss = {
                            onEvent(OnboardingFinished)
                            expandedState.value = false
                        }
                    )
                }
            }
        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
    add {
        val (currentOnboarding) = appbarState.keySelectionOnboarding
        val expandedState =
            remember { mutableStateOf(currentOnboarding == OnBoarding.KEY_CREATION) }
        Box {
            Icon(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                contentDescription = null,
                modifier = Modifier.clickable {
                    onEvent(KeySelectorEvent.KeyStoreCreateClicked)
                }
            )
            androidx.compose.animation.AnimatedVisibility(expandedState.value) {
                Tooltip(
                    onboardingInfo = appbarState.keySelectionOnboarding,
                    onDismiss = {
                        onEvent(OnboardingFinished)
                        expandedState.value = false
                    }
                )
            }
        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
}

@Composable
fun fileBrowserSet(
    appbarState: AppbarState.Browser,
    onEvent: (FileBrowserEvent) -> Unit,
    spotLightOffsetState: MutableState<Offset?>
): Set<Action> = buildSet {
    Log.e("@@@", "${appbarState.fileBrowserOnboarding.first}")
    add {
        AnimatedVisibility(appbarState.hasSelected) {
            Text(
                text = appbarState.selectedCount.toString(),
                color = Color.Green,
            )
        }
    }
    add {
        AnimatedVisibility(appbarState.hasSelected) {
            val (currentOnboarding) = appbarState.fileBrowserOnboarding
            val expandedState =
                remember { mutableStateOf(currentOnboarding == OnBoarding.ADD_SELECTED) }
            Box {
                Icon(
                    painterResource(ru.barinov.core.R.drawable.baseline_check_24),
                    null,
                    modifier = Modifier.clickable {
                        onEvent(FileBrowserEvent.AddSelectionClicked)
                    }
                )
                androidx.compose.animation.AnimatedVisibility(expandedState.value) {
                    Tooltip(
                        onboardingInfo = appbarState.fileBrowserOnboarding,
                        onDismiss = {
                            onEvent(OnboardingFinished)
                            expandedState.value = false
                        }
                    )
                }
            }
        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
    add {
        val (currentOnboarding) = appbarState.fileBrowserOnboarding
        val expandedState =
            remember { mutableStateOf(currentOnboarding == OnBoarding.REMOVE_SELECTED) }
        AnimatedVisibility(appbarState.hasSelected) {
            Icon(
                painterResource(ru.barinov.core.R.drawable.baseline_clear_24),
                null,
                modifier = Modifier.clickable { onEvent(RemoveSelection) }
            )
        }
    }
    add {
        Spacer(modifier = Modifier.width(16.dp))
    }
    add {
        AnimatedVisibility(appbarState.hasSelected) {
            val (currentOnboarding) = appbarState.fileBrowserOnboarding
            val expandedState =
                remember { mutableStateOf(currentOnboarding == OnBoarding.REMOVE_SELECTED) }
            Icon(
                painter = painterResource(id = ru.barinov.core.R.drawable.baseline_delete_outline_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onEvent(DeleteSelected)
                    }
                    .size(26.dp),
                tint = Color.Black
            )

        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
    add {
        AnimatedVisibility(appbarState.sourceState.isMsdAttached && !appbarState.hasSelected) {
            val (currentOnboarding) = appbarState.fileBrowserOnboarding
            val expandedState =
                remember { mutableStateOf(currentOnboarding == OnBoarding.CHANGE_SOURCE) }
            Box {
                Icon(
                    painter = painterResource(
                        id = if (appbarState.sourceState.currentSource == Source.INTERNAL)
                            ru.barinov.core.R.drawable.baseline_sd_storage_24
                        else ru.barinov.core.R.drawable.mass_storage_device
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            onEvent(SourceChanged)
                        }
                        .size(26.dp),
                    tint = if (appbarState.sourceState.currentSource == Source.INTERNAL) Color.Black else LocalContentColor.current
                )
            }
            AnimatedVisibility(expandedState.value) {
                Tooltip(
                    onboardingInfo = appbarState.fileBrowserOnboarding,
                    onDismiss = {
                        if (!appbarState.fileBrowserOnboarding.second) {
                            spotLightOffsetState.value = null
                        }
                        onEvent(OnboardingFinished)
                        expandedState.value = false
                    }
                )
            }
        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
    add {
        AnimatedVisibility(!appbarState.isPageEmpty && !appbarState.hasSelected) {
            val (currentOnboarding) = appbarState.fileBrowserOnboarding
            val expandedState =
                remember { mutableStateOf(currentOnboarding == OnBoarding.SORT_FILES) }
            val sortDropDownExpanded = remember { mutableStateOf(false) }
            Box {
                Icon(
                    painter = painterResource(id = ru.barinov.core.R.drawable.outline_sort_24),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { sortDropDownExpanded.value = true }
                        .size(26.dp),
                    tint = Color.Black
                )
                SortDropDownMenu(
                    isExpanded = sortDropDownExpanded.value,
                    selectedSort = appbarState.selectedSortType,
                    onDismissRequest = { sortDropDownExpanded.value = false },
                    onEvent = { onEvent(it) }
                )
                androidx.compose.animation.AnimatedVisibility(expandedState.value) {
                    Tooltip(
                        offset = DpOffset(128.dp, 12.dp),
                        onboardingInfo = appbarState.fileBrowserOnboarding,
                        onDismiss = {
                            onEvent(OnboardingFinished)
                            expandedState.value = false
                        }
                    )
                }
            }
        }
    }
    add { Spacer(modifier = Modifier.width(16.dp)) }
}
