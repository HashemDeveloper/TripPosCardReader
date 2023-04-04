package com.limosys.test.tripostestapp.ui.screens.sales

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.limosys.test.tripostestapp.objects.TriPOSTransactionType
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.vantiv.triposmobilesdk.*
import com.vantiv.triposmobilesdk.enums.*
import com.vantiv.triposmobilesdk.exceptions.StoredTransactionNotFoundException
import com.vantiv.triposmobilesdk.express.CreditCardReversalMessage
import com.vantiv.triposmobilesdk.express.Terminal
import com.vantiv.triposmobilesdk.requests.CreditCardAdjustmentRequest
import com.vantiv.triposmobilesdk.requests.RefundRequest
import com.vantiv.triposmobilesdk.requests.ReturnRequest
import com.vantiv.triposmobilesdk.requests.ReversalRequest
import com.vantiv.triposmobilesdk.requests.SaleRequest
import com.vantiv.triposmobilesdk.responses.RefundResponse
import com.vantiv.triposmobilesdk.responses.ReturnResponse
import com.vantiv.triposmobilesdk.responses.ReversalResponse
import com.vantiv.triposmobilesdk.responses.SaleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject


@HiltViewModel
class SalesViewModel @Inject constructor(application: Application): AndroidViewModel(application), SaleRequestListener,
    RefundRequestListener, ReversalRequestListener,
    DeviceInteractionListener, ReturnListener {
    lateinit var sharedVtp: VTP
        internal set
    private lateinit var device: Device

    private var isSalesProcessing: Boolean = false

    private val _salesState: MutableStateFlow<SalesState> = MutableStateFlow(SalesState.None)
    val salesState: StateFlow<SalesState> = _salesState

    private val _debugState: MutableStateFlow<DebugState> = MutableStateFlow(DebugState.None)
    val debugState: StateFlow<DebugState> = _debugState

    private val detailList: MutableList<String> = arrayListOf()


    private fun addToList(message: String) {
        this.detailList.add(message)
        this._debugState.value = DebugState.DebugList(this.detailList)
    }

    fun handleEvent(state: SalesState) {
        when (state) {
            is SalesState.SetupPayment -> {
                initializePaymentType(state)
            }
            is SalesState.None -> {
                this._salesState.value = SalesState.None
            }
            else -> {}
        }
    }

    private fun initializePaymentType(state: SalesState) {
        if (!this.sharedVtp.isInitialized) {
            addToList("Not Initialized!")
            return
        }
        this.device = this.sharedVtp.device
        initializeCardInputReader(state)
    }

    private fun initializeCardInputReader(state: SalesState) {
        when (state) {
            is SalesState.SetupPayment -> {
                try {
                    if (!isSalesProcessing) {
                        try {
                            if (sharedVtp.allStoredTransactions.size > 0) {
                                sharedVtp.allStoredTransactions.forEach {
                                    sharedVtp.allStoredTransactions.remove(it)
                                }
                            }
                        } catch (e: StoredTransactionNotFoundException) {
                            e.printStackTrace()
                        }
                        sharedVtp.setStatusListener {
                            when (it) {
                                VtpStatus.RunningSale -> {
                                    print("running...")
                                }
                                VtpStatus.ProcessingCardInput -> {
                                    print("Card input")
                                }
                                VtpStatus.SendingToHost -> {
                                    print("Sending to host")
                                }
                                VtpStatus.GettingContinuingEmvTransaction -> {
                                    print("EMV")
                                }
                                else -> {}
                            }
                        }
                        when (state.transactionType) {
                            TriPOSTransactionType.SALE.type -> {
                                addToList("Initializing sales request...")
                                sharedVtp.processSaleRequest(setupSaleRequest(state.amount), this@SalesViewModel, this@SalesViewModel)
                            }
                            TriPOSTransactionType.REFUND.type -> {
                                addToList("Initialized refund request...")
                                sharedVtp.processRefundRequest(setupRefundRequest(state.amount, state.saleResponse), this@SalesViewModel, this@SalesViewModel)
                            }
                            TriPOSTransactionType.REVERSAL.type -> {
                                addToList("Initialized reversal request. Please press Pay to process reversal.")
                                sharedVtp.processReversalRequest(setupReversalRequest(state.amount, state.saleResponse), this@SalesViewModel)
                            }
                            TriPOSTransactionType.RETURN.type -> {
                                addToList("Initialized return request. Please press Pay to process return.")
                                sharedVtp.processReturnRequest(setupReturnRequest(state.amount, state.saleResponse), this@SalesViewModel)
                            }
                        }
                    } else {
                        addToList("Processing payment...")
                    }
                    sharedVtp.statusListener = VtpProcessStatusListener {
                        isSalesProcessing = it.statusOrderPosition != 0
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace()
                    }
                }
            }
            else -> {}
        }
    }

    private fun setupReturnRequest(amount: Double, saleResponse: SaleResponse?): ReturnRequest? {
        val returnRequest = ReturnRequest()
        saleResponse?.let {
            returnRequest.paymentType = it.paymentType
            returnRequest.transactionID = it.host?.transactionID
            returnRequest.referenceNumber = it.referenceNumber
        }
        returnRequest.laneNumber = "1"
        returnRequest.transactionAmount = BigDecimal(amount)
        returnRequest.cardPresentCode= Terminal.CardPresentCode.Present
        return returnRequest
    }

    private fun setupReversalRequest(amount: Double, saleResponse: SaleResponse?): ReversalRequest {
        val reversalRequest = ReversalRequest()
        saleResponse?.let {
            reversalRequest.transactionId = it.host?.transactionID
            reversalRequest.ebtType = it.ebtType
            reversalRequest.paymentType = it.paymentType
            reversalRequest.referenceNumber= it.referenceNumber
        }
        reversalRequest.reversalType = ReversalType.System
        reversalRequest.laneNumber = "1"
        reversalRequest.transactionAmount = BigDecimal(amount)
        reversalRequest.cardholderPresentCode = CardHolderPresentCode.Present
        return reversalRequest
    }

    private fun setupRefundRequest(amount: Double, saleResponse: SaleResponse?): RefundRequest {
        val refundRequest = RefundRequest()
        saleResponse?.let {
            refundRequest.referenceNumber = it.referenceNumber
        }
        refundRequest.laneNumber = "1"
        refundRequest.transactionAmount = BigDecimal(amount)
        refundRequest.cardholderPresentCode =  CardHolderPresentCode.Present
        return refundRequest
    }

    private fun setupSaleRequest(amount: Double): SaleRequest {
        val saleRequest = SaleRequest()
        saleRequest.laneNumber = "1"
        saleRequest.referenceNumber = uniqueNumericValue(16)
        saleRequest.ticketNumber = uniqueNumericValue(6)
        saleRequest.transactionAmount = BigDecimal(amount)
//        saleRequest.tipAmount = BigDecimal(2.0)
        saleRequest.cardholderPresentCode = CardHolderPresentCode.Present
        return saleRequest
    }

    private fun uniqueNumericValue(range: Int): String {
        val uuid = UUID.randomUUID()
        val uuidString = uuid.toString().replace("-", "")
        return uuidString.substring(0, range)
    }

    override fun onAmountConfirmation(
        type: AmountConfirmationType?,
        p1: BigDecimal?,
        p2: DeviceInteractionListener.ConfirmAmountListener?
    ) {
        p2?.confirmAmount(true)
        addToList("Amount Confirmation Type: ${type?.name}\nAmount:$p1")
    }

    override fun onChoiceSelections(
        p0: Array<out String>?,
        p1: SelectionType?,
        p2: DeviceInteractionListener.SelectChoiceListener?
    ) {
        print(p0)
    }

    override fun onNumericInput(
        p0: NumericInputType?,
        p1: DeviceInteractionListener.NumericInputListener?
    ) {
        addToList("Numeric Input Type: ${p0?.name}")
    }

    override fun onSelectApplication(
        p0: Array<out String>?,
        p1: DeviceInteractionListener.SelectChoiceListener?
    ) {
        print(p0)
    }

    override fun onPromptUserForCard(p0: String?) {
        addToList(p0 ?: "")
    }

    override fun onDisplayText(p0: String?) {
        addToList(p0 ?: "")
    }

    override fun onRemoveCard() {
       print("Remove the card")
        addToList("Remove the Card...")
    }

    override fun onCardRemoved() {
        print("Card removed!")
        addToList("Card removed")
        this.device.reset()
    }

    override fun onSaleRequestCompleted(saleResponse: SaleResponse?) {
        val gson = Gson()
        Log.i("TriposSale", gson.toJson(saleResponse))
        print(saleResponse?.emv?.cryptogram)
        this._salesState.value = SalesState.Completed(saleResponse)
        addToList("Sales Response: ${saleResponse?.transactionStatus}")
    }

    override fun onSaleRequestError(error: Exception?) {
        addToList("Sale Request Error ${error?.message}")
        this._salesState.value = SalesState.Error(error?.message)
    }

    override fun onRefundRequestCompleted(refundRes: RefundResponse?) {
        print(refundRes)
        this._salesState.value = SalesState.Completed(null)
        addToList("Sales Response: ${refundRes?.transactionStatus}")
    }

    override fun onRefundRequestError(errorRes: java.lang.Exception?) {
        print(errorRes)
        addToList("Refund Request Error ${errorRes?.message}")
        this._salesState.value = SalesState.Error(errorRes?.message)
    }

    override fun onReversalRequestCompleted(reversalRes: ReversalResponse?) {
        print(reversalRes)
        this._salesState.value = SalesState.Completed(null)
        addToList("Sales Response: ${reversalRes?.transactionStatus}")
    }

    override fun onReversalRequestError(errorRes: java.lang.Exception?) {
        print(errorRes)
        addToList("Reversal Request Error ${errorRes?.message}")
        this._salesState.value = SalesState.Error(errorRes?.message)
    }

    override fun onReturnRequestCompleted(returnRes: ReturnResponse?) {
        print(returnRes)
        this._salesState.value = SalesState.Completed(null)
        addToList("Sales Response: ${returnRes?.transactionStatus}")
    }

    override fun onReturnRequestError(errorRes: java.lang.Exception?) {
        print(errorRes)
        addToList("Reversal Request Error ${errorRes?.message}")
        this._salesState.value = SalesState.Error(errorRes?.message)
    }
}
