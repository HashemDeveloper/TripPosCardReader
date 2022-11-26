package com.limosys.test.tripostestapp.ui.screens.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.limosys.test.tripostestapp.ui.screens.states.SalesState
import com.vantiv.triposmobilesdk.*
import com.vantiv.triposmobilesdk.enums.AmountConfirmationType
import com.vantiv.triposmobilesdk.enums.NumericInputType
import com.vantiv.triposmobilesdk.enums.SelectionType
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

    fun handleEvent(state: SalesState) {
        when (state) {
            is SalesState.SwipeOrTap -> {
                if (!this.sharedVtp.isInitialized) {
                    return
                }
                this.device = this.sharedVtp.device
                if (this.device !is CardInputDevice) {
                    return
                }
                initializeCardInputReader()
            }
            is SalesState.None -> {
                this._salesState.value = SalesState.None
            }
            else -> {}
        }
    }
    private fun initializeCardInputReader() {
        try {
            (device as CardInputDevice).enableCardInput(this, this)
        } catch (e: DeviceNotConnectedException) {
            e.printStackTrace()
        } catch (e: DeviceNotInitializedException) {
            e.printStackTrace()
        } catch (e: CardInputEnableException) {
            e.printStackTrace()
        }
    }

    override fun onInputTimeout(p0: TimeoutException?) {
        print(p0?.message ?: "")
    }

    override fun onCardInputCompleted(data: CardData?) {
        this._salesState.value = SalesState.Swiped(data)
        initializeCardInputReader()
    }

    override fun onCardInputError(p0: Exception?) {
        print(p0?.message ?: "")
    }

    override fun onAmountConfirmation(
        p0: AmountConfirmationType?,
        p1: BigDecimal?,
        p2: DeviceInteractionListener.ConfirmAmountListener?
    ) {
        print(p0?.name)
    }

    override fun onChoiceSelections(
        p0: Array<out String>?,
        p1: SelectionType?,
        p2: DeviceInteractionListener.SelectChoiceListener?
    ) {
        print(p0?.size)
    }

    override fun onNumericInput(
        p0: NumericInputType?,
        p1: DeviceInteractionListener.NumericInputListener?
    ) {
        print(p0?.name)
    }

    override fun onSelectApplication(
        p0: Array<out String>?,
        p1: DeviceInteractionListener.SelectChoiceListener?
    ) {
        print(p0?.size)
    }

    override fun onPromptUserForCard(p0: String?) {
        print(p0)
    }

    override fun onDisplayText(p0: String?) {
        print(p0)
    }

    override fun onRemoveCard() {
       print("Remove the card")
    }

    override fun onCardRemoved() {
        print("Card removed!")
    }
}
