package ru.barinov.file_browser

import androidx.lifecycle.ViewModel
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked

class ContainersViewModel(

): ViewModel() {

    fun handleEvent(event: ContainersEvent){
        when(event){
            OnBackPressed -> TODO()
            is OnFileClicked -> TODO()
        }
    }
}