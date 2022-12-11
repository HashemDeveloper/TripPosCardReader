package com.limosys.test.tripostestapp.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.component.DebugButton
import com.limosys.test.tripostestapp.component.DisplayDebugList
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.vantiv.triposmobilesdk.CardData
import kotlinx.coroutines.delay

@Composable
fun SalesScreen(
    navController: NavHostController,
    state: SalesState,
    handleEvent: (SalesState) -> Unit,
    debugState: DebugState,
) {
    var isDebugClicked by rememberSaveable {
        mutableStateOf(false)
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        if (isDebugClicked) {
            DebugList(state = debugState)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SalesStatus(state, handleEvent)
        }
        DisplayDebugButton {
            isDebugClicked = true
        }
    }
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
fun SalesStatus(state: SalesState, handleEvent: (SalesState) -> Unit) {
    var defaultState by remember {
        mutableStateOf(true)
    }
    when (state) {
        is SalesState.None -> {
            Text(text = stringResource(id = R.string.swipe_or_tap))
            handleEvent(SalesState.SwipeOrTap)
        }
        is SalesState.Swiped -> {
            val data: CardData? = state.name
            data?.entryMode?.name?.let { Text(text = it) }
            LaunchedEffect(key1 = defaultState, block = {
                delay(5000)
                handleEvent(SalesState.None)
                defaultState = false
            })
        }
        else -> {}
    }
}