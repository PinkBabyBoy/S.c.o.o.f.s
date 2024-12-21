package ru.barinov.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.barinov.core.ui.TextEnter
import ru.barinov.settings.R
import ru.barinov.settings.Setting

@Composable
fun SettingButton(model: Setting.TextButton){
    ElevatedCard(Modifier.clickable{model.onClicked()}) {
        Row(Modifier.padding(vertical = 24.dp, horizontal = 16.dp).fillMaxWidth()){
            Text(stringResource(model.titleRes), Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
fun SettingSwitch(model: Setting.SwitchButton){
    ElevatedCard {
        Row(Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth()){
            val switchState = remember { mutableStateOf(model.switchState) }
            Text(stringResource(model.titleRes), Modifier.align(Alignment.CenterVertically).weight(3f).padding(start = 16.dp))
            Switch(switchState.value, onCheckedChange = {
                model.onSwitchChanges(it)
                switchState.value = it
            },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SettingTextEnter(model: Setting.TextEnterButton){
    ElevatedCard() {
        val text = remember { mutableStateOf(model.savedText) }
        Row(Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth()){
            TextEnter(
                onValueChanged = {
                    text.value = it
                    model.onTextChanges(text.value)
                },
                supportText = {
                    Text("dfgdfg")
                },
                isError = false,
                modifier = Modifier.align(Alignment.CenterVertically).weight(3f).padding(start = 16.dp, top = 8.dp)
            )
            if(model.switchState != null){
                Switch(
                    checked =  model.switchState,
                    onCheckedChange = model.onSwitchStateChanges,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically).padding(bottom = 8.dp))
            }
        }
    }
}



@Composable
@Preview
fun SettingSwitchPreview(){
    SettingSwitch(Setting.SwitchButton(ru.barinov.core.R.string.start, true, {}))
}

@Composable
@Preview
fun SettingTextEnterPreview(){
    SettingTextEnter(Setting.TextEnterButton(ru.barinov.core.R.string.start,true, "JOPA", {}, {}))
}

@Composable
@Preview
fun SettingButtonPreview(){
    SettingButton(Setting.TextButton(ru.barinov.core.R.string.start, {}))
}