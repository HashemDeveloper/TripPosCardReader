package com.limosys.test.tripostestapp.ui.screens.states

import com.vantiv.triposmobilesdk.VTP

sealed class InitializationState {
    class SdkInitializationSuccess(val sharedVtp: VTP) : InitializationState()
    class SdkInitializationException(val message: String): InitializationState()
    class DeviceConnectionError(errorMessage: String) : InitializationState()
    class DeviceWarning(warningMessage: String) : InitializationState()
    object None : InitializationState()
    object InitializeSdk : InitializationState()
    object DeviceConnected: InitializationState()
    object DeviceDisconnected : InitializationState()
    object DeviceBatteryLow : InitializationState()
}
