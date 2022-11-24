package com.limosys.test.tripostestapp.base

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.vantiv.triposmobilesdk.Device
import com.vantiv.triposmobilesdk.DeviceConnectionListener
import java.lang.Exception

abstract class BaseActivity:ComponentActivity(), DeviceConnectionListener {

    override fun onConnected(p0: Device?, p1: String?, p2: String?, p3: String?) {
        displayToastMessage("Device Connected")
    }

    override fun onDisconnected(p0: Device?) {
        displayToastMessage("Disconnected")
    }

    override fun onError(p0: Exception?) {
        displayToastMessage(p0?.message ?: "")
        print(p0?.message)
    }

    override fun onBatteryLow() {
        displayToastMessage("Low Battery")
    }

    override fun onWarning(p0: Exception?) {
        displayToastMessage(p0?.message ?: "")
    }
    private fun displayToastMessage(message: String) {
        runOnUiThread  {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}