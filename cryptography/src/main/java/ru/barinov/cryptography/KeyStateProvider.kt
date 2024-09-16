package ru.barinov.cryptography

import kotlinx.coroutines.flow.StateFlow

interface KeyStateProvider {

    val isKeyLoaded: StateFlow<Boolean>
}
