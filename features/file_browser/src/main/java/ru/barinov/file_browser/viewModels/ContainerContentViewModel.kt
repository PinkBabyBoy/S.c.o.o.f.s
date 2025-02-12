package ru.barinov.file_browser.viewModels


import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.SideEffect
import ru.barinov.file_browser.usecases.OpenContainerUseCase

//TODO complete
class ContainerContentViewModel(
    containerName: String,
    openContainerUseCase: OpenContainerUseCase
) : SideEffectViewModel<SideEffect>() {

    private val _uiState: MutableStateFlow<ContainerContentViewState> =
        MutableStateFlow(ContainerContentViewState.Loading)
    val viewState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                emit(openContainerUseCase(containerName).flowOn(Dispatchers.IO).map {})
            }
                .catch {
                    _uiState.emit(ContainerContentViewState.Error)
                }
                .collectLatest { }
        }

    }

}


sealed interface ContainerContentViewState {
    data object Loading : ContainerContentViewState
    data class ContainerLoaded(val pageDataFlow: Flow<PagingData<>>) : ContainerContentViewState
    data object Error : ContainerContentViewState
}