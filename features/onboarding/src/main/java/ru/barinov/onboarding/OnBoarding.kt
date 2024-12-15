package ru.barinov.onboarding

import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import ru.barinov.core.headerDefault
import ru.barinov.core.ui.bsContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoarding(
    title: String,
    state: TooltipState,
    tooltipText: String,
    onClick: () -> Unit = {},
    width: Dp,
    hasNext: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    TooltipBox(
        modifier = modifier.width(width),
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                colors = TooltipDefaults.richTooltipColors().copy(containerColor = bsContainerColor),
                title = { Text(title, style = headerDefault())},
                action = {
                    Box(Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = onClick,
                            Modifier.align(Alignment.BottomEnd)
                        ) { Text(
                            if (hasNext)
                                stringResource(ru.barinov.core.R.string.next_onb_button_text)
                            else stringResource(ru.barinov.core.R.string.close_onb_button_text),
                            style = headerDefault()
                        ) }
                    }
                }
            ) { Text(tooltipText, fontSize = 14.sp) }
        },
        state = state
    ) { content() }
}
