package ru.barinov.onboarding


import android.annotation.SuppressLint
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.ColorUtils
import ru.barinov.core.R

@Composable
//Use only inside Box
fun Tooltip(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    properties: PopupProperties = PopupProperties(focusable = true),
    onboardingInfo: OnboardingInfo,
    offset: DpOffset = DpOffset(1.dp, 1.dp),
    onDismiss: (OnBoarding) -> Unit
) {
    if (onboardingInfo.first == null) return
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = true

    if (expandedStates.currentState || expandedStates.targetState) {
//        if (expandedStates.isIdle) {
//            LaunchedEffect(timeoutMillis, expanded) {
//                delay(timeoutMillis)
//                expanded.value = false
//            }
//        }

        Popup(
            onDismissRequest = { onDismiss(onboardingInfo.first!!) },
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset(20, 220)
                }
            },
            properties = properties,
        ) {
            Box(
                // Add space for elevation shadow
                modifier = Modifier.padding(TooltipElevation.dp),
            ) {
                TooltipContent(
                    expandedStates = expandedStates,
                    backgroundColor = backgroundColor,
                    modifier = modifier,
                    onboardingInfo = onboardingInfo,
                    offset = offset,
                    onDismiss = onDismiss
                )
            }
        }
    }
}


@Composable
private fun TooltipContent(
    expandedStates: MutableTransitionState<Boolean>,
    backgroundColor: Color,
    modifier: Modifier,
    onboardingInfo: OnboardingInfo,
    onDismiss: (OnBoarding) -> Unit,
    offset: DpOffset
) {
    // Tooltip open/close animation.
    val transition = rememberTransition(expandedStates, "Tooltip")

    val alpha = transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = InTransitionDuration)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) { if (it) 1f else 0f }

    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.contentColorFor(backgroundColor)
                .takeOrElse { backgroundColor.onColor() }),
        modifier = Modifier
            .alpha(1f)
            .padding(start = offset.x, end = offset.y)
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = TooltipElevation.dp),
    ) {
        val p = TooltipPadding.dp
        val (onboarding, hasNext) = onboardingInfo
        Row(
            modifier = Modifier
                .padding(start = p, top = p * 0.5f, end = p, bottom = p * 0.7f),
            content = {
                val text = getOnboardingText(onboarding!!)
                Text(text, modifier = Modifier.weight(6f))
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    contentDescription = null,
                    painter = painterResource(if (hasNext) ru.barinov.core.R.drawable.baseline_arrow_back_24 else ru.barinov.core.R.drawable.baseline_clear_24),
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .rotate(if (hasNext) 180f else 0f)
                        .clickable { onDismiss(onboardingInfo.first!!) }
                )
            },
        )
    }
}

@Composable
//TODO implement me
private fun getOnboardingText(onBoarding: OnBoarding): String {
    val res = when (onBoarding) {
        OnBoarding.KEY_CREATION -> R.string.select_folder_warning
        OnBoarding.ADD_SELECTED -> R.string.select_folder_warning
        OnBoarding.CREATE_CONTAINER -> R.string.select_folder_warning
        OnBoarding.CHANGE_SOURCE -> R.string.select_folder_warning
        OnBoarding.SORT_FILES -> R.string.select_folder_warning
        OnBoarding.SELECT_FILE -> R.string.select_folder_warning
        OnBoarding.REMOVE_SELECTED -> R.string.select_folder_warning
    }
    return stringResource(res)
}


private const val TooltipElevation = 16
private const val TooltipPadding = 16

// Tooltip open/close animation duration.
private const val InTransitionDuration = 64
private const val OutTransitionDuration = 240

// Default timeout before tooltip close
//private const val TooltipTimeout = 2_000L - OutTransitionDuration


// Color utils

/**
 * Calculates an 'on' color for this color.
 *
 * @return [Color.Black] or [Color.White], depending on [isLightColor].
 */
fun Color.onColor(): Color {
    return if (isLightColor()) Color.Black else Color.White
}

/**
 * Calculates if this color is considered light.
 *
 * @return true or false, depending on the higher contrast between [Color.Black] and [Color.White].
 *
 */
fun Color.isLightColor(): Boolean {
    val contrastForBlack = calculateContrastFor(foreground = Color.Black)
    val contrastForWhite = calculateContrastFor(foreground = Color.White)
    return contrastForBlack > contrastForWhite
}

fun Color.calculateContrastFor(foreground: Color): Double {
    return ColorUtils.calculateContrast(foreground.toArgb(), toArgb())
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview(showBackground = true)
fun Preview() {
    Tooltip(
        onboardingInfo = OnBoarding.SORT_FILES to false,
        onDismiss = {}, offset = DpOffset(1.dp, 1.dp)
    )
}