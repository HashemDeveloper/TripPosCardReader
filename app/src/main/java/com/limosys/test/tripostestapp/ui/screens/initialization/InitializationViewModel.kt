package com.limosys.test.tripostestapp.ui.screens.initialization

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.utils.TriposConfig
import com.vantiv.triposmobilesdk.BuildConfig
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
                    _initializationState.value = InitializationState.SdkInitializationSuccess(sharedVtp)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        print(e.message)
                    }
                    _initializationState.value = InitializationState.SdkInitializationException(e.message)
                }
            }
        }
    }

    override fun onConnected(p0: Device?, p1: String?, p2: String?, p3: String?) {
        debug("Connected")
        this._initializationState.value = InitializationState.DeviceConnected
    }

    override fun onDisconnected(p0: Device?) {
        debug("Disconnected")
        this._initializationState.value = InitializationState.DeviceDisconnected
    }

    override fun onError(p0: Exception?) {
        debug(p0?.message ?: "")
        this._initializationState.value = InitializationState.DeviceConnectionError(p0?.message ?: "")
    }

    override fun onBatteryLow() {
        debug("Low Batter")
        this._initializationState.value = InitializationState.DeviceBatteryLow
    }

    override fun onWarning(p0: Exception?) {
        debug(p0?.message ?: "")
        this._initializationState.value = InitializationState.DeviceWarning(p0?.message ?: "")
    }
    private fun debug(message: String) {
        if (BuildConfig.DEBUG) {
            print(message)
        }
    }
}