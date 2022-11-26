package com.limosys.test.tripostestapp.ui.screens.states

sealed class InitializationState {
    object None : InitializationState()
    object InitializeSdk : InitializationState()
}
