package com.limosys.test.tripostestapp.ui.screens.initialization

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.utils.TriposConfig
import com.vantiv.triposmobilesdk.Device
import com.vantiv.triposmobilesdk.DeviceConnectionListener
import com.vantiv.triposmobilesdk.VTP
import com.vantiv.triposmobilesdk.triPOSMobileSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class InitializationViewModel @Inject constructor(application: Application): AndroidViewModel(application), DeviceConnectionListener {

    private val sharedVtp: VTP = triPOSMobileSDK.getSharedVtp()
    private val _initializationState: MutableStateFlow<InitializationState> = MutableStateFlow(InitializationState.None)
    val initializationState: StateFlow<InitializationState> = _initializationState

    fun handleEvents(state: InitializationState) {
        when (state) {
            is InitializationState.InitializeSdk -> {
                initializeSdk()
            }
            else -> {}
        }
    }

    private fun initializeSdk() {
        if (!this.sharedVtp.isInitialized) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    sharedVtp.initialize(getApplication() as Context, TriposConfig.getSharedConfig(), this@InitializationViewModel)
                } catch (e: Exception) {
                    print(e.message)
                }
            }
        }
    }

    override fun onConnected(p0: Device?, p1: String?, p2: String?, p3: String?) {
       print("Connected")
    }

    override fun onDisconnected(p0: Device?) {
        print("Disconnected")
    }

    override fun onError(p0: Exception?) {
        print(p0?.message ?: "")
    }

    override fun onBatteryLow() {
        print("Low Batter")
    }

    override fun onWarning(p0: Exception?) {
        print(p0?.message ?: "")
    }
}