package com.limosys.test.tripostestapp.ui.screens.sales

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.IntentFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.component.DebugButton
import com.limosys.test.tripostestapp.component.DisplayDebugList
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.limosys.test.tripostestapp.utils.*
import com.vantiv.triposmobilesdk.CardData
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Identifier

@Composable
fun SalesScreen(
    navController: NavHostController,
    state: SalesState,
    handleEvent: (SalesState) -> Unit,
    debugState: DebugState,
    initializationState: InitializationState
) {
    var isDebugClicked by rememberSaveable {
        mutableStateOf(false)
    }
    SetupBroadCastListener{

    }
    Surface(modifier = Modifier.fillMaxSize()) {
        if (isDebugClicked) {
            DebugList(state = debugState)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SalesStatus(state, handleEvent, initializationState) {
                navController.popBackStack()
            }
        }
        DisplayDebugButton {
            isDebugClicked = true
        }
    }
}

@Composable
private fun SetupBroadCastListener(onBlueToothStateChanged: (String) -> Unit) {
    val context: Context = LocalContext.current
    val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    context.registerReceiver(broadCastReceiver {bluetoothState ->
        when (bluetoothState) {
            BluetoothAdapter.STATE_OFF -> {
                onBlueToothStateChanged.invoke("Bluetooth off")
            }
            BluetoothAdapter.STATE_ON -> {
                onBlueToothStateChanged.invoke("Bluetooth on")
            }
        }
    }, intentFilter)
}

@Composable
private fun DebugList(state: DebugState) {
    when (state) {
        is DebugState.DebugList -> {
            val list: MutableList<String> = state.list
            DisplayDebugList(detailList = list)
        }
        else ->{}
    }
}
@Composable
private fun DisplayDebugButton(onDebugClicked: () -> Unit) {
    DebugButton {
        onDebugClicked.invoke()
    }
}
@Composable
fun SalesStatus(
    state: SalesState,
    handleEvent: (SalesState) -> Unit,
    initializationState: InitializationState,
    handleInitializationEvent: () -> Unit,
) {
    var defaultState by remember {
        mutableStateOf(true)
    }
    var isHideRow by rememberSaveable {
        mutableStateOf(false)
    }

    when (initializationState) {
        InitializationState.DeviceDisconnected -> {
            Text(text = "Disconnected")
            Button(onClick = {
                handleInitializationEvent.invoke()
            }) {
                Text(text = "Reconnect")
            }
        }
        else -> {}
    }
    when (state) {
        is SalesState.None -> {
            if (isHideRow) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        isHideRow = false
                    }) {
                        Text(text = "Done")
                    }
                }

            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        handleEvent.invoke(SalesState.SetupPayment)
                        isHideRow = true
                    }) {
                        Text(text = "Pay")
                    }
                }
            }
        }
        is SalesState.Completed -> {
            LaunchedEffect(key1 = defaultState, block = {
                delay(5000)
                handleEvent(SalesState.None)
                defaultState = false
            })
        }
        else -> {}
    }
}