package com.limosys.test.tripostestapp.ui.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.limosys.test.tripostestapp.R
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.vantiv.triposmobilesdk.CardData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SalesScreen(
    navController: NavHostController,
    state: SalesState,
    handleEvent: (SalesState) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SalesStatus(state, handleEvent)
        }
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
