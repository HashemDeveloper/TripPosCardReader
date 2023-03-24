package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.objects.ComposeData
import com.limosys.test.tripostestapp.utils.MessageState
import kotlinx.coroutines.launch

@Composable
fun MessageHandler(state: MessageState) {
    var message = ""
    when (state) {
        is MessageState.INFO -> {
            message = state.message
            print(message)
        }
        is MessageState.WARNING -> {
            message = state.message
            DisplaySnackBar(data = ComposeData(actionText = stringResource(id = R.string.action_ok), message = message))
        }
        is MessageState.ALERT -> {
            message = state.message
            print(message)
        }
        is MessageState.ERROR -> {
            message = state.message
            print(message)
        }
    }
}

@Composable
fun DisplaySnackBar(data: ComposeData) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ) {
        LaunchedEffect(key1 = true, block = {
            coroutineScope.launch {
                val snackBarResult = scaffoldState.snackbarHostState.showSnackbar(
                    message = data.message,
                    actionLabel = data.actionText
                )
                when (snackBarResult) {
                    SnackbarResult.Dismissed -> {
                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    }
                    SnackbarResult.ActionPerformed -> {}
                }
            }
        })
    }
}