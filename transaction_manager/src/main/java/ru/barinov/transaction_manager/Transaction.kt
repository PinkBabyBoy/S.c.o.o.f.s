package ru.barinov.transaction_manager


import kotlinx.coroutines.flow.MutableSharedFlow
import ru.barinov.core.FileEntity
import java.util.UUID

class Transaction(
    val uuid: UUID,
    val progressFlow: MutableSharedFlow<Long>,
    val files: List<FileEntity>,
    val containerData: ContainerData,
) {

    var state = State.IDLE
        private set

    internal fun changeState(state: State){
        this.state = state
    }

    enum class State {
        IDLE, STARTED, FINISHED
    }
}
