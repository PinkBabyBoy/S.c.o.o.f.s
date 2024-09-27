package ru.barinov.file_browser.usecases

import ru.barinov.file_browser.ContainersManager

class CreateContainerUseCase(
    private val containersManager: ContainersManager
) {

    suspend operator fun invoke(name: String): Result<Unit> = runCatching {
        containersManager.addContainer(name)
    }
}
