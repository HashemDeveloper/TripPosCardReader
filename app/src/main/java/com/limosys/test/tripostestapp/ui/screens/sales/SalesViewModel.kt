package com.limosys.test.tripostestapp.ui.screens.sales

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limosys.test.tripostestapp.repo.TriposDataStoreRepo
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.vantiv.triposmobilesdk.*
import com.vantiv.triposmobilesdk.enums.*
import com.vantiv.triposmobilesdk.exceptions.StoredTransactionNotFoundException
import com.vantiv.triposmobilesdk.requests.SaleRequest
import com.vantiv.triposmobilesdk.responses.SaleResponse
import com.vantiv.triposmobilesdk.storeandforward.StoredTransactionRecord
import com.vantiv.triposmobilesdk.storeandforward.StoredTransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeoutException
import javax.inject.Inject


@HiltViewModel
class SalesViewModel @Inject constructor(application: Application): AndroidViewModel(application), SaleRequestListener, DeviceInteractionListener {
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
            SalesState.SetupPayment -> {
                try {
                    if (!isSalesProcessing) {
                        addToList("Initializing sales request...")
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
                            }
                        }
                        sharedVtp.processSaleRequest(setupSaleRequest(2.00), this@SalesViewModel, this@SalesViewModel)
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

    private fun setupSaleRequest(amount: Double): SaleRequest {
        val saleRequest = SaleRequest()
        saleRequest.transactionAmount = BigDecimal(amount)
        saleRequest.cardholderPresentCode = CardHolderPresentCode.Present
        return saleRequest
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
        print(saleResponse)
        this._salesState.value = SalesState.Completed
        addToList("Sales Response: ${saleResponse?.transactionStatus}")
    }

    override fun onSaleRequestError(error: Exception?) {
        addToList("Sale Request Error ${error?.message}")
        this._salesState.value = SalesState.Error(error?.message)
    }
}
