package ru.barinov.protected_enter

import androidx.compose.runtime.Stable

@Stable
internal data class EnterScreenUiState(
    val stage: Stage,
    val errors: List<ErrorType>,
    val hasPermission: Boolean,
    val biometricAuthState: BiometricAuthState,
    //TODO Prefs
    val bioAuthEnabled: Boolean = true
) {

    fun onPermissionGiven(hasPassword: Boolean, hasRequiredPermission: Boolean) = copy(
        stage = if (hasPassword) Stage.Enter else Stage.Create,
        hasPermission = hasRequiredPermission
    )

    fun errors(errors: List<ErrorType>) = copy(errors = errors)

    fun onBioAuthStateChanged(state: BiometricAuthState) = copy(biometricAuthState = state)

    companion object {
        fun empty(): EnterScreenUiState = EnterScreenUiState(Stage.Create, emptyList(), false, BiometricAuthState.Idle)
        fun construct(hasPassword: Boolean, hasRequiredPermission: Boolean): EnterScreenUiState =
            EnterScreenUiState(
                stage = if (hasPassword) Stage.Enter else Stage.Create,
                errors = emptyList(),
                hasPermission = hasRequiredPermission,
                biometricAuthState = BiometricAuthState.Idle
            )
    }
}

internal fun EnterScreenUiState.isBioAuthAvailable(): Boolean {
  return bioAuthEnabled && biometricAuthState == BiometricAuthState.Idle && stage == Stage.Enter
}

internal enum class BiometricAuthState {
    Failed,
    Success,
    Idle
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
