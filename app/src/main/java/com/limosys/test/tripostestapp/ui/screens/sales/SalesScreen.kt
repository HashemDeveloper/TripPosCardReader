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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.component.DebugButton
import com.limosys.test.tripostestapp.component.DisplayDebugList
import com.limosys.test.tripostestapp.component.TriposOutLinedInputField
import com.limosys.test.tripostestapp.component.TriposSingleButtonDialog
import com.limosys.test.tripostestapp.component.styles.Spacing
import com.limosys.test.tripostestapp.objects.TriPOSTransactionType
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.limosys.test.tripostestapp.utils.*
import com.vantiv.triposmobilesdk.responses.SaleResponse
import kotlinx.coroutines.delay

@Composable
fun SalesScreen(
    navController: NavHostController,
    state: SalesState,
    handleEvent: (SalesState) -> Unit,
    debugState: DebugState,
    initializationState: InitializationState
) {
    var isDebugClicked by rememberSaveable {
        mutableStateOf(true)
    }
    SetupBroadCastListener{

    }
    Surface(modifier = Modifier.fillMaxSize()) {
        if (isDebugClicked) {
            DebugList(state = debugState)
        }
        Column(
            modifier = Modifier.padding(top = Spacing.MEDIUM_18_DP.space),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SalesStatus(state, handleEvent, initializationState) {
                navController.popBackStack()
            }
        }
        DisplayDebugButton {
            isDebugClicked = !isDebugClicked
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
    var displayTransactionRequestDialog by remember {
        mutableStateOf(TriPOSTransactionType.NONE)
    }
    var amountState by remember {
        mutableStateOf("")
    }
    var formError by remember {
        mutableStateOf(false)
    }
    var displayRefundReversalButton by remember {
        mutableStateOf(false)
    }
    var saleResponse: SaleResponse ?= null
    var saleResponseState by remember {
        mutableStateOf(saleResponse)
    }

    if (displayTransactionRequestDialog.isShow) {
        TriposSingleButtonDialog(
            modifier = Modifier.padding(all = Spacing.SMALL_16_DP.space),
            title = "Process ${displayTransactionRequestDialog.type}",
            actionButtonText = stringResource(id = R.string.action_ok),
            content = {
                Text(text = "")
                TriposOutLinedInputField(
                    value = amountState,
                    isError = formError,
                    onValueChanged = {
                       amountState = it
                    },
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    hint = "Enter amount",
                    label = if (formError) "Amount is required!" else "Enter Amount"
                )
            },
            onDismissRequest = {
                displayTransactionRequestDialog = TriPOSTransactionType.NONE
            }) {
            formError = amountState.isEmpty()
            if (formError.not()) {
                handleEvent.invoke(SalesState.SetupPayment(displayTransactionRequestDialog.type, amountState.toDouble(), saleResponseState))
                displayTransactionRequestDialog = TriPOSTransactionType.NONE
            }
        }
    }

    when (initializationState) {
        is InitializationState.DeviceDisconnected -> {
            Text(text = "Disconnected")
            Button(onClick = {
                handleInitializationEvent.invoke()
            }) {
                Text(text = "Reconnect")
            }
        }
        is InitializationState.DeviceConnectionError -> {
            handleEvent.invoke(SalesState.Recover)
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
                DisplayTransactionButtons(onSaleClicked = {
                    isHideRow = true
                    displayTransactionRequestDialog = TriPOSTransactionType.SALE
                }, onRefundClicked = {
                    isHideRow = true
                    displayTransactionRequestDialog = TriPOSTransactionType.REFUND
                }, onReversalClicked = {
                    isHideRow = true
                    displayTransactionRequestDialog = TriPOSTransactionType.REVERSAL
                }, onReturnClicked = {
                    isHideRow = true
                    displayTransactionRequestDialog = TriPOSTransactionType.RETURN
                }, displayRefundReversalButton = displayRefundReversalButton)
            }
        }
        is SalesState.Completed -> {
            LaunchedEffect(key1 = defaultState, block = {
                delay(5000)
                handleEvent(SalesState.None)
                defaultState = false
                displayRefundReversalButton = true
                saleResponse = state.saleResponse
                saleResponseState = saleResponse
            })
        }
        is SalesState.Error -> {
            DisplayTransactionButtons(onSaleClicked = {
                isHideRow = true
                displayTransactionRequestDialog = TriPOSTransactionType.SALE
            }, onRefundClicked = {
                isHideRow = true
                displayTransactionRequestDialog = TriPOSTransactionType.REFUND
            }, onReversalClicked = {
                isHideRow = true
                displayTransactionRequestDialog = TriPOSTransactionType.REVERSAL
            }, onReturnClicked = {
                isHideRow = true
                displayTransactionRequestDialog = TriPOSTransactionType.RETURN
            }, displayRefundReversalButton =  displayRefundReversalButton)
        }
        else -> {}
    }
}

@Composable
private fun DisplayTransactionButtons(
    onSaleClicked: () -> Unit,
    onRefundClicked: () -> Unit,
    onReversalClicked: () -> Unit,
    onReturnClicked: () -> Unit,
    displayRefundReversalButton: Boolean
) {
    Column(
        modifier = Modifier.padding(
            start = Spacing.SMALL_16_DP.space,
            end = Spacing.SMALL_16_DP.space
        ),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
               onSaleClicked.invoke()
            }) {
            Text(text = "Pay")
        }
        if (displayRefundReversalButton) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onRefundClicked.invoke()
                }) {
                Text(text = "Refund")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onReversalClicked.invoke()
                }) {
                Text(text = "Reversal")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onReturnClicked.invoke()
                }) {
                Text(text = "Return")
            }
        }
    }
}
