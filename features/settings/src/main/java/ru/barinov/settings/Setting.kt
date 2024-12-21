package ru.barinov.settings

import androidx.annotation.StringRes

sealed class Setting(@StringRes val titleRes: Int) {
    class TextButton(@StringRes textRes: Int, val onClicked: () -> Unit) : Setting(textRes)
    class SwitchButton(
        @StringRes textRes: Int,
        val switchState: Boolean,
        val onSwitchChanges: (Boolean) -> Unit
    ) : Setting(textRes)
    class TextEnterButton(
        @StringRes textRes: Int,
        val switchState: Boolean?,
        val savedText: String,
        val onTextChanges: (String) -> Unit,
        val onSwitchStateChanges: (Boolean) -> Unit
    ) : Setting(textRes)
}