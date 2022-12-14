package com.limosys.test.tripostestapp.ui.screens.initialization

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.component.DebugButton
import com.limosys.test.tripostestapp.component.DisplayDebugList
import com.limosys.test.tripostestapp.component.MessageHandler
import com.limosys.test.tripostestapp.component.StandardDivider
import com.limosys.test.tripostestapp.ui.routes.AppRoutes
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.utils.*
import kotlinx.coroutines.flow.StateFlow


@Composable
fun InitializationScreen(
    navController: NavHostController,
    state: InitializationState,
    handleEvent: (InitializationState) -> Unit,
    showDetails: StateFlow<MutableList<String>>
) {
    val detailList: MutableList<String> = showDetails.collectAsState().value
    val context: Context = LocalContext.current
    val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    var buttonText by rememberSaveable {
        mutableStateOf("")
    }
    var deviceIdentifier by rememberSaveable {
        mutableStateOf("")
    }
    context.registerReceiver(broadCastReceiver{ bluetoothState ->
        when (bluetoothState) {
            BluetoothAdapter.STATE_OFF -> {
                buttonText = "Turn Bluetooth On"
            }
            BluetoothAdapter.STATE_ON -> {
                buttonText = "Scan"
            }
        }
    }, intentFilter)

    when (state) {
        is InitializationState.ConnectToDevice -> {
            buttonText = "Connect"
            val identifier = state.identifier
            deviceIdentifier = identifier
        }
        is InitializationState.SdkInitializationSuccess -> {
            buttonText = "Connecting.."
        }
        is InitializationState.DeviceConnectionError -> {
            buttonText = "Retry"
        }
        else -> {}
    }
    MainContent(
        state,
        buttonText,
        onConnected = {
            navController.navigate(AppRoutes.SALES_SCREEN.name + "/$deviceIdentifier")
        },
        detailList,
    ) {
        if (buttonText.isEmpty() || buttonText == "Scan") {
            handleEvent.invoke(InitializationState.ScanBlueTooth)
        } else if (buttonText == "Connect" || buttonText == "Connecting.." || buttonText == "Retry") {
            handleEvent.invoke(InitializationState.ConnectToDevice(deviceIdentifier))
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent(
    state: InitializationState,
    buttonText: String,
    onConnected: () -> Unit,
    detailList: MutableList<String>,
    handleEvent: () -> Unit
) {
    val permissionsToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    } else {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    var displayDebug by rememberSaveable {
        mutableStateOf(false)
    }
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
    ) {
        if (displayDebug) {
            DebugList(detailList)
            StandardDivider()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DisplayStatus(state) {
                onConnected.invoke()
            }
            InitializeSdkButton(buttonText, permissionsToCheck) {
                handleEvent.invoke()
            }
        }
        DisplayDebugButton {
            displayDebug = true
        }
    }
}
@Composable
private fun DebugList(list: MutableList<String>) {
    DisplayDebugList(detailList = list)
}
@Composable
private fun DisplayDebugButton(onDebugClicked: () -> Unit) {
    DebugButton {
       onDebugClicked.invoke()
    }
}
@Composable
fun DisplayStatus(state: InitializationState, onConnected: () -> Unit) {
    val context: Context = LocalContext.current
    when (state) {
        is InitializationState.BlueToothScanInitialized -> {
            DisplayMessage(message = stringResource(id = R.string.turn_on_triPOS_bluetooth_device))
        }
        is InitializationState.BluetoothScanRequestError -> {
            val message: String = state.errorMessage
            DisplayMessage(message = message)
        }
        is InitializationState.SdkInitializationSuccess -> {
            val identifier: String = state.deviceIdentifier
            val message = "Please wait while connecting to $identifier"
            DisplayMessage(message = message)
        }
        is InitializationState.SdkInitializationException -> {
            val message = state.message
            DisplayMessage(message = message)
        }
        is InitializationState.DeviceConnected -> {
           LaunchedEffect(key1 = true, block = {
               onConnected.invoke()
           })
        }
        is InitializationState.DeviceDisconnected -> {
            DisplayMessage(message = stringResource(id = R.string.disconnected))
            val intent = Intent(DEVICE_DISCONNECTED)
            context.sendBroadcast(intent)
        }
        is InitializationState.DeviceConnectionError -> {
            val message: String = state.errorMessage
            DisplayMessage(message = message)
            val intent = Intent(DEVICE_ERROR)
            intent.putExtra(DEVICE_ERROR, state.errorMessage)
            context.sendBroadcast(intent)
        }
        is InitializationState.DeviceBatteryLow -> {
            MessageHandler(state = MessageState.WARNING(stringResource(id = R.string.battery_low)))
        }
        is InitializationState.DeviceWarning -> {
            val warning: String = state.warningMessage
            MessageHandler(state = MessageState.WARNING(warning))

            val intent = Intent(DEVICE_WARNING)
            intent.putExtra(DEVICE_WARNING, state.warningMessage)
            context.sendBroadcast(intent)
        }
        is InitializationState.EnableBlueTooth -> {
            NavigateToBluetoothSettings()
        }
        is InitializationState.ConnectToDevice -> {
            val identifier = state.identifier
            val message = "Detected BBPosDevice with identifier: $identifier"
            DisplayMessage(message = message)
        }
        else -> {}
    }
}

@Composable
private fun NavigateToBluetoothSettings() {
    val context: Context = LocalContext.current
    val intentOpenBluetoothSettings = Intent()
    intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
    context.startActivity(intentOpenBluetoothSettings)
}

@Composable
private fun DisplayMessage(message: String) {
    Text(text = message, textAlign = TextAlign.Center)
}

@Composable
private fun RetryButton(onRetryClicked: () -> Unit) {
    Button(onClick = { onRetryClicked.invoke() }) {
        Text(text = "Retry")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun InitializeSdkButton(buttonText: String, permissionsToCheck: MultiplePermissionsState, handleEvent: () -> Unit) {
    if (permissionsToCheck.allPermissionsGranted) {
        DisplayActionButton(buttonText.ifEmpty { getTitle() }) {
            handleEvent.invoke()
        }
    } else {
        getTextToShowGivenPermissions(
            permissionsToCheck.revokedPermissions, permissionsToCheck.shouldShowRationale
        )
        Button(onClick = {
            permissionsToCheck.launchMultiplePermissionRequest()
        }) {
            Text(text = "Request Permission")
        }
    }
}
@Composable
private fun getTitle(): String {
    val context: Context = LocalContext.current
    return if (isBluetoothEnabled(context)) return "Scan" else "Turn Bluetooth On"
}
@Composable
private fun DisplayActionButton(title: String, onClicked: () -> Unit) {
    Button(onClick = {
        onClicked.invoke()
    }) {
        Text(text = title)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>, shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The ")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            " important. Please grant all of them for the app to function properly."
        } else {
            " denied. The app cannot function without them."
        }
    )
    return textToShow.toString()
}

@Preview
@Composable
fun DisplayPreview() {
    MainContent(state = InitializationState.None,
        "",
        onConnected = {},
        detailList = arrayListOf()) {

    }
}