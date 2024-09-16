package ru.barinov.file_browser

import ru.barinov.external_data.MassStorageEventBus

class GetMSDAttachStateProvider(
    private val massStorageEventBus: MassStorageEventBus
) {

    operator fun invoke() =
        massStorageEventBus.massStorageState
}
