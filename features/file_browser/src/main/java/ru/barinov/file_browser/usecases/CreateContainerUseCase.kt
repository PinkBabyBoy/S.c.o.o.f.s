package ru.barinov.file_browser.usecases

import ru.barinov.file_browser.ContainersManager

class CreateContainerUseCase(
    private val containersManager: ContainersManager,
    private val getCurrentKeyHashUseCase: GetSerializableCurrentKeyHashUseCase
) {

    suspend operator fun invoke(name: String): Result<Unit> = runCatching {
        containersManager.addContainer(name, getCurrentKeyHashUseCase())
    }.also { it.getOrThrow() }
}
