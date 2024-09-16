package ru.barinov.file_browser

import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.external_data.MassStorageState

class IsMSDAttachedUseCase(
    private val massStorageEventBus: MassStorageEventBus
) {

    operator fun invoke(): Boolean =
        massStorageEventBus.massStorageState.value is MassStorageState.Ready

}
