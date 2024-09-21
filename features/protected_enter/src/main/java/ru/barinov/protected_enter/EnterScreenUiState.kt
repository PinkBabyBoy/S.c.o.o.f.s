package ru.barinov.protected_enter

import androidx.compose.runtime.Stable

@Stable
internal data class EnterScreenUiState(
    val type: Stage,
    val errors: List<ErrorType>,
    val hasPermission: Boolean
) {

    fun onPermissionGiven(hasPassword: Boolean, hasRequiredPermission: Boolean) = copy(
        type = if (hasPassword) Stage.Enter else Stage.Create,
        hasPermission = hasRequiredPermission
    )

    fun errors(errors: List<ErrorType>) = copy(errors = errors)

    companion object {
        fun empty(): EnterScreenUiState = EnterScreenUiState(Stage.Create, emptyList(), false)
        fun construct(hasPassword: Boolean, hasRequiredPermission: Boolean): EnterScreenUiState =
            EnterScreenUiState(
                type = if (hasPassword) Stage.Enter else Stage.Create,
                errors = emptyList(),
                hasPermission = hasRequiredPermission
            )
    }
}

internal enum class Stage {
    Enter,
    Create
}

internal enum class ErrorType {
    READ_HASH_ERROR,
    WRONG_PASSWORD,
    CREATE_NOT_EQUALS,
    PASSWORD_EMPTY,
    CHECK_EMPTY
}

internal sealed interface SideEffects {

    data object EnterGranted : SideEffects

    data object AskPermission : SideEffects

    data object ProgressStop: SideEffects
}
