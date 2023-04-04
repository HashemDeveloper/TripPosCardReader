package com.limosys.test.tripostestapp.ui.screens.initialization

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bbpos.bbdevice.BBDeviceController.BBDeviceControllerListener
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.limosys.test.tripostestapp.objects.DeviceObj
import com.limosys.test.tripostestapp.repo.ISharedPref
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.utils.TriposConfig
import com.limosys.test.tripostestapp.utils.isBluetoothEnabled
import com.vantiv.triposmobilesdk.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class InitializationViewModel @Inject constructor(application: Application, private val iSharedPref: ISharedPref): AndroidViewModel(application), DeviceConnectionListener, BluetoothScanRequestListener {
    lateinit var sharedVtp: VTP
        internal set
    private val _initializationState: MutableStateFlow<InitializationState> = MutableStateFlow(InitializationState.None)
    val initializationState: StateFlow<InitializationState> = _initializationState

    private val _showDetails: MutableStateFlow<MutableList<String>> = MutableStateFlow(arrayListOf())
    val showDetails: StateFlow<MutableList<String>> = _showDetails

    private val detailList: MutableList<String> = arrayListOf()
    private val gson = Gson()
    init {
        getStoredDeviceIdentifier()
    }
    private fun getStoredDeviceIdentifier() {
        val identifier: String = this.iSharedPref.getIdentifier()
        if (identifier.isNotEmpty()) {
            val deviceObj: DeviceObj = this.gson.fromJson(identifier, DeviceObj::class.java)
            if (deviceObj.devices.size > 1) {
                addToList("Multiple Device Detected ${deviceObj.devices}")
                this._initializationState.value = InitializationState.PromptDialog(deviceObj.devices)
            } else {
                _initializationState.value = InitializationState.StoredDeviceIdentifier(deviceObj.devices[0])
            }
        } else {
            _initializationState.value = InitializationState.Start
        }
    }
    fun handleEvents(state: InitializationState) {
        when (state) {
            is InitializationState.ScanBlueTooth -> {
                if (isBluetoothEnabled(getApplication())) {
                    scanBluetooth()
                } else {
                    this._initializationState.value = InitializationState.EnableBlueTooth
                }
            }
            is InitializationState.ConnectToDevice -> {
                if (isBluetoothEnabled(getApplication())) {
                    initializeSdk(state.identifier)
                }
            }
            is InitializationState.DebugClicked -> {
                this._initializationState.value = InitializationState.DisplayDetails(this.detailList)
            }
            else -> {}
        }
    }
    private fun addToList(message: String) {
        this.detailList.add(message)
        this._showDetails.value = this@InitializationViewModel.detailList
    }
    private fun scanBluetooth() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                this@InitializationViewModel.sharedVtp.scanBluetoothDevicesWithConfiguration(getApplication() as Context, TriposConfig.getSharedConfig(""), this@InitializationViewModel)
                _initializationState.value = InitializationState.BlueToothScanInitialized
                addToList("Scanning Bluetooth...")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    print(e.message)
                }
                _initializationState.value = InitializationState.SdkInitializationException(e.message ?: "")
                addToList("Error scanning. Exception (${e.message})")
            }
        }
    }
    private fun initializeSdk(identifier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sharedVtp.initialize(getApplication() as Context, TriposConfig.getSharedConfig(identifier), this@InitializationViewModel)
                _initializationState.value = InitializationState.SdkInitializationSuccess(identifier)
                addToList("Finished Initializing SDK")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    print(e.message)
                }
                _initializationState.value = InitializationState.SdkInitializationException(e.message ?: "")
                addToList("Error Initializing SDK. Exception: (${e.message})")
            }
        }
    }

    override fun onConnected(device: Device?, description: String?, model: String?, serialNumber: String?) {
        debug("Connected")
        addToList("Connected")
        val deviceObj = DeviceObj()
        val identifier: String = this.iSharedPref.getIdentifier()
        val deviceList: ArrayList<String> = if (identifier.isNotEmpty()) {
            try {
                val obj: DeviceObj = this.gson.fromJson(identifier, DeviceObj::class.java)
                obj.devices
            } catch (e: JsonSyntaxException) {
                ArrayList()
            }
        } else {
            ArrayList()
        }

        if (!deviceList.contains(serialNumber)) {
            deviceList.add(serialNumber ?: "")
        }

        deviceObj.devices = deviceList
        val jsonDeviceObj = gson.toJson(deviceObj)
        this.iSharedPref.setIdentifier(jsonDeviceObj)

        this._initializationState.value = InitializationState.DeviceConnected
    }

    override fun onDisconnected(p0: Device?) {
        debug("Disconnected")
        addToList("Disconnected")
        this._initializationState.value = InitializationState.DeviceDisconnected
    }

    override fun onError(p0: Exception?) {
        debug(p0?.message ?: "")
        this._initializationState.value = InitializationState.DeviceConnectionError(p0?.message ?: "")
        if (this.sharedVtp.isInitialized) {
            sharedVtp.deinitialize()
        }
        addToList("Error connecting to Device. Details: (${p0?.message})")
    }

    override fun onBatteryLow() {
        debug("Battery Low")
        this._initializationState.value = InitializationState.DeviceBatteryLow
        addToList("Battery Low...")
    }

    override fun onWarning(p0: Exception?) {
        debug(p0?.message ?: "")
        this._initializationState.value = InitializationState.DeviceWarning(p0?.message ?: "")
        addToList("Warning: Issue: (${p0?.message})")
    }
    private fun debug(message: String) {
        if (BuildConfig.DEBUG) {
            print(message)
        }
    }

    /**
     * @param bArray returns list of BBPosDevices
     */
    override fun onScanRequestCompleted(bArray: ArrayList<String>?) {
        bArray?.let {
            if (it.size > 1) {
                addToList("Multiple Device Detected $it")
                this._initializationState.value = InitializationState.PromptDialog(it)
            } else {
                val identifier: String = it[0]
                addToList("Detected BBPosDevice with identifier: $identifier")
                this._initializationState.value = InitializationState.ConnectToDevice(identifier)
            }
        }
    }

    override fun onScanRequestError(p0: Exception?) {
        this._initializationState.value = InitializationState.BluetoothScanRequestError(p0?.message ?: "")
    }
}