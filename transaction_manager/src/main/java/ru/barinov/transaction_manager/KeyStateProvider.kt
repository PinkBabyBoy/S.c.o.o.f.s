package ru.barinov.transactionsmanager

import kotlinx.coroutines.flow.StateFlow

interface KeyStateProvider {

    val isKeyLoaded: StateFlow<Boolean>
}
