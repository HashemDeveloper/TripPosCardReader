package com.limosys.test.tripostestapp.ui.screens.states

import com.vantiv.triposmobilesdk.CardData
import com.vantiv.triposmobilesdk.responses.SaleResponse

sealed class SalesState {
    class Swiped(val name: CardData?) : SalesState()
    object None : SalesState()

    object Processing: SalesState()
    class SetupPayment(val transactionType: String, val amount: Double, val saleResponse: SaleResponse?): SalesState()
    class Completed(val saleResponse: SaleResponse?) : SalesState()
    class Error(message: String?): SalesState()
    object Recover: SalesState()
}
