package ru.barinov.protected_enter

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.barinov.cryptography.hash.HashCreator
import ru.barinov.cryptography.hash.HashMode
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.password_manager.PType
import ru.barinov.password_manager.PasswordStorage
import ru.barinov.permission_manager.PermissionChecker
import ru.barinov.protected_enter.fingerprint.BiometricEnterHelper
import ru.barinov.transaction_manager.Cleaner

internal class EnterScreenViewModel(
    private val hashCreator: HashCreator,
    private val hashValidator: HashValidator,
    private val passwordStorage: PasswordStorage,
    private val cleaner: Cleaner,
    private val permissionChecker: PermissionChecker,
    private val biometricEnterHelper: BiometricEnterHelper
) : ViewModel() {

    private val userInput = MutableLiveData<CharArray>()
    private val userCheckPassword = MutableLiveData<CharArray>()

    private val _sideEffects = Channel<SideEffects>(capacity = Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val _uiState = MutableStateFlow(reconstructUiState())
    val uiState = _uiState.asStateFlow()

    fun handleEvent(event: EnterScreenEvent) {
        when (event) {
            is EnterScreenEvent.NewInput -> newUserInput(event.type, event.input)
            EnterScreenEvent.SubmitClicked -> submit()
            EnterScreenEvent.PermissionGranted
            -> _uiState.value = _uiState.value.onPermissionGiven(
                hasPassword = passwordStorage.hasPasswordSet(),
                hasRequiredPermission = permissionChecker.hasPermissionToRead()
            )

            EnterScreenEvent.ResetConfirmed -> resetPassword()
            is EnterScreenEvent.RequestFingerprintAuth -> requestFingerprintAuth(event.fragmentActivity)
        }
    }

    private fun requestFingerprintAuth(fragmentActivity: FragmentActivity) {
        biometricEnterHelper.startFingerprintAuth(fragmentActivity, {
            viewModelScope.launch { _sideEffects.send(SideEffects.EnterGranted) }
        }, {
            _uiState.value = uiState.value.onBioAuthStateChanged(BiometricAuthState.Failed)
        }
        )
    }

    private fun resetPassword() {
        cleaner.clearStoredData()
        passwordStorage.clear()
        _uiState.value = reconstructUiState()
    }

    private fun reconstructUiState() =
        EnterScreenUiState.construct(
            hasPassword = passwordStorage.hasPasswordSet(),
            hasRequiredPermission = permissionChecker.hasPermissionToRead()
        )


    private fun submit() {
        viewModelScope.launch(Dispatchers.Default) {
            if (!permissionChecker.hasPermissionToRead()) {
                requestPermission()
                return@launch
            }
            if (!passwordStorage.hasPasswordSet()) {
                createPassword()
            } else {
                submitPassword()
            }
        }
    }


    private suspend fun createPassword() {
        val password = userInput.value
        val check = userCheckPassword.value
        val errors = checkInputsOnCreate(password, check)
        if (errors.isNotEmpty()) {
            viewModelScope.launch {
                onError(errors)
            }
            return
        }
        val hash = hashCreator.createHash(password!!, HashMode.PASSWORD)
        passwordStorage.store(hash, PType.REAL)
        _sideEffects.send(SideEffects.EnterGranted)
    }

    private fun checkInputsOnCreate(
        password: CharArray?,
        check: CharArray?
    ): List<ErrorType> {
        val errors = mutableListOf<ErrorType>()
        if (password == null || password.isEmpty()) {
            errors.add(ErrorType.PASSWORD_EMPTY)
        }
        if (check == null || check.isEmpty()) {
            errors.add(ErrorType.CHECK_EMPTY)
        }
        if (!password.contentEquals(check)) {
            errors.add(ErrorType.CREATE_NOT_EQUALS)
        }
        return errors
    }

    private fun newUserInput(type: InputType, input: CharSequence) {
        when (type) {
            InputType.PASSWORD -> userInput.value = input.toList().toCharArray()
            InputType.CONFIRMATION -> userCheckPassword.value = input.toList().toCharArray()
        }
    }

    private fun onError(errors: List<ErrorType>) {
        _uiState.value = uiState.value.errors(errors)
        viewModelScope.launch { _sideEffects.send(SideEffects.ProgressStop) }
    }

    private suspend fun submitPassword() {

        suspend fun validateRPass(storedHash: ByteArray) {
            val enteredPassword = userInput.value
            if (enteredPassword == null || enteredPassword.isEmpty()) {
                onError(listOf(ErrorType.PASSWORD_EMPTY))
                return
            }
            val result = hashValidator.validate(storedHash, enteredPassword)
            if (result) {
                _sideEffects.send(SideEffects.EnterGranted)
            } else {
                onError(listOf(ErrorType.WRONG_PASSWORD))
            }
        }

        fun validateBoth(storedHashR: ByteArray, storedHashE: ByteArray) {
            val enteredPassword = userInput.value ?: return
            viewModelScope.launch(Dispatchers.Default) {
                val resultE = hashValidator.validate(storedHashE, enteredPassword)
                val resultR = hashValidator.validate(storedHashR, enteredPassword)
                when {
                    resultE -> {
                        cleaner.clearStoredData()
                        _sideEffects.send(SideEffects.EnterGranted)
                    }

                    resultR -> {
                        _sideEffects.send(SideEffects.EnterGranted)
                    }

                    else -> {
                        onError(listOf(ErrorType.WRONG_PASSWORD))
                    }
                }
            }
        }
        runCatching {
            val r = passwordStorage.readHash(PType.REAL)
            val e = passwordStorage.readHash(PType.EMERGENCY)
            when {
                r == null -> error("Wrong state")
                e == null -> validateRPass(r)
                else -> validateBoth(r, e)
            }
        }.onFailure {
            Log.d("@@@", "${it.stackTraceToString()}")
            onError(listOf(ErrorType.READ_HASH_ERROR))
        }
    }

    private suspend fun requestPermission() {
        _sideEffects.send(SideEffects.AskPermission)
    }
}

internal enum class InputType {
    PASSWORD, CONFIRMATION
}

internal sealed interface EnterScreenEvent {

    class NewInput(val type: InputType, val input: CharSequence) : EnterScreenEvent

    data object SubmitClicked : EnterScreenEvent

    data object PermissionGranted : EnterScreenEvent

    data object ResetConfirmed : EnterScreenEvent

    class RequestFingerprintAuth(val fragmentActivity: FragmentActivity): EnterScreenEvent

}
