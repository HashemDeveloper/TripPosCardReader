package com.limosys.test.tripostestapp.ui.screens.states

import com.vantiv.triposmobilesdk.CardData

sealed class SalesState {
    class Swiped(val name: CardData?) : SalesState()
    object None : SalesState()

    object Processing: SalesState()
    object SetupPayment: SalesState()
    object Completed : SalesState()
    class Error(message: String?): SalesState()
    object Recover: SalesState()
}
