package com.limosys.test.tripostestapp.ui.screens.initialization

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.component.MessageHandler
import com.limosys.test.tripostestapp.ui.routes.AppRoutes
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.utils.MessageState


@Composable
fun InitializationScreen(
    navController: NavHostController,
    state: InitializationState,
    handleEvent: (InitializationState) -> Unit
) {
    MainContent(state, onConnected = {
        navController.navigate(AppRoutes.SALES_SCREEN.name)
    }) {
        handleEvent.invoke(InitializationState.InitializeSdk)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent(state: InitializationState,onConnected: () -> Unit, handleEvent: () -> Unit) {
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
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DisplayStatus(state) {
                onConnected.invoke()
            }
            InitializeSdkButton(permissionsToCheck) {
                handleEvent.invoke()
            }
        }
    }
}

@Composable
fun DisplayStatus(state: InitializationState, onConnected: () -> Unit) {
    when (state) {
        is InitializationState.SdkInitializationSuccess -> {
            DisplayMessage(message = stringResource(id = R.string.turn_on_triPOS_bluetooth_device))
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
        }
        is InitializationState.DeviceConnectionError -> {
            val message: String = state.errorMessage
            DisplayMessage(message = message)
        }
        is InitializationState.DeviceBatteryLow -> {
            MessageHandler(state = MessageState.WARNING(stringResource(id = R.string.battery_low)))
        }
        is InitializationState.DeviceWarning -> {
            val warning: String = state.warningMessage
            MessageHandler(state = MessageState.WARNING(warning))
        }
        else -> {}
    }
}
@Composable
private fun DisplayMessage(message: String) {
    Text(text = message, textAlign = TextAlign.Center)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun InitializeSdkButton(permissionsToCheck: MultiplePermissionsState, handleEvent: () -> Unit) {
    if (permissionsToCheck.allPermissionsGranted) {
        handleEvent.invoke()
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