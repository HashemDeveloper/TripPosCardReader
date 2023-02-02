package com.limosys.test.tripostestapp.ui.screens.states

import com.vantiv.triposmobilesdk.CardData

sealed class SalesState {
    class Swiped(val name: CardData?) : SalesState()
    object None : SalesState()
    object SwipeToPay : SalesState()
    object TapToPay : SalesState()

    object SetupPayment: SalesState()
}
