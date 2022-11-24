package com.limosys.test.tripostestapp.base

import androidx.activity.ComponentActivity
import com.vantiv.triposmobilesdk.Device
import com.vantiv.triposmobilesdk.DeviceConnectionListener
import java.lang.Exception

abstract class BaseActivity:ComponentActivity(), DeviceConnectionListener {

    override fun onConnected(p0: Device?, p1: String?, p2: String?, p3: String?) {
        print("Device connected")
    }

    override fun onDisconnected(p0: Device?) {
        print("Disconnected")
    }

    override fun onError(p0: Exception?) {
        print(p0?.message)
    }

    override fun onBatteryLow() {
        print("Low Battery")
    }

    override fun onWarning(p0: Exception?) {
        print(p0?.message)
    }
}