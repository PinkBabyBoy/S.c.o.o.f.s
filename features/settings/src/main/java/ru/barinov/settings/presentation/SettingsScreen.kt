package ru.barinov.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ru.barinov.settings.R
import ru.barinov.settings.Setting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingList: List<Setting>){
    Column {
        TopAppBar( {
            Text(stringResource(ru.barinov.core.R.string.key_not_loaded_containers))
        })
        LazyColumn {
            items(settingList){ item ->
                when(item){
                    is Setting.SwitchButton -> SettingSwitch(item)
                    is Setting.TextButton -> SettingButton(item)
                    is Setting.TextEnterButton -> SettingTextEnter(item)
                }
                HorizontalDivider(color = Color.Black)
            }
        }
    }
}
