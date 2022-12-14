package com.limosys.test.tripostestapp.ui.screens.sales

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.limosys.test.tripostestapp.utils.ReflectionUtils.recursiveToString
import com.vantiv.triposmobilesdk.*
import com.vantiv.triposmobilesdk.enums.AmountConfirmationType
import com.vantiv.triposmobilesdk.enums.NumericInputType
import com.vantiv.triposmobilesdk.enums.SelectionType
import com.vantiv.triposmobilesdk.enums.TransactionType
import com.vantiv.triposmobilesdk.exceptions.CardInputEnableException
import com.vantiv.triposmobilesdk.exceptions.DeviceNotConnectedException
import com.vantiv.triposmobilesdk.exceptions.DeviceNotInitializedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import java.util.concurrent.TimeoutException
import javax.inject.Inject


@HiltViewModel
class SalesViewModel @Inject constructor(application: Application): AndroidViewModel(application), DeviceInteractionListener, CardInputListener {
    private var sharedVtp: VTP = triPOSMobileSDK.getSharedVtp()
    private lateinit var device: Device
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
            is SalesState.SwipeToPay -> {
                initializePaymentType(state)
            }
            is SalesState.TapToPay -> {
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
        if (this.device !is CardInputDevice) {
            addToList("Invalid card input device!")
            return
        }
        initializeCardInputReader(state)
    }

    private fun initializeCardInputReader(state: SalesState) {
        try {

            when (state) {
                SalesState.SwipeToPay -> {
                    addToList("Enabling swipe to pay Card Input Listeners...")
                    (device as CardInputDevice).enableCardInput(this, this)
                }
                SalesState.TapToPay -> {
                    addToList("Enabling tap to pay Card Input Listeners...")
                    (device as CardInputDevice).enableCardInput("Tap To Pay", false, false, true, true, true, TransactionType.Sale, BigDecimal(0.5), BigDecimal(0.5), this, this)
                }
                else -> {}
            }
        } catch (e: DeviceNotConnectedException) {
            addToList(e.message ?: "")
            e.printStackTrace()
        } catch (e: DeviceNotInitializedException) {
            addToList(e.message ?: "")
            e.printStackTrace()
        } catch (e: CardInputEnableException) {
            addToList(e.message ?: "")
            e.printStackTrace()
        }
    }

    override fun onInputTimeout(p0: TimeoutException?) {
        addToList(p0?.message ?: "")
        print(p0?.message ?: "")
    }

    override fun onCardInputCompleted(data: CardData?) {
        addToList("Card Actions:${data?.entryMode}")
        this._salesState.value = SalesState.Swiped(data)
        var output = ""
        try {
            output = recursiveToString(data)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val cardOutput = "Card Output:\n$output"
        addToList(cardOutput)
    }

    override fun onCardInputError(p0: Exception?) {
        print(p0?.message ?: "")
        addToList(p0?.message ?: "")
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
}
