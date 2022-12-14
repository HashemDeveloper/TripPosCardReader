package com.limosys.test.tripostestapp.ui.screens.states


sealed class InitializationState {
    class SdkInitializationSuccess(val deviceIdentifier: String) : InitializationState()
    class SdkInitializationException(val message: String): InitializationState()
    class DeviceConnectionError(val errorMessage: String) : InitializationState()
    class DeviceWarning(val warningMessage: String) : InitializationState()
    class BluetoothScanRequestError(val errorMessage: String) : InitializationState()
    class DisplayDetails(val detailList: MutableList<String>) : InitializationState()
    class ConnectToDevice(val identifier: String) : InitializationState()
    object None : InitializationState()
    object InitializeSdk : InitializationState()
    object DeviceConnected: InitializationState()
    object DeviceDisconnected : InitializationState()
    object DeviceBatteryLow : InitializationState()
    object ScanBlueTooth : InitializationState()
    object BlueToothScanInitialized : InitializationState()
    object DebugClicked : InitializationState()
    object EnableBlueTooth : InitializationState()
}
