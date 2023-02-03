package com.limosys.test.tripostestapp.utils

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


const val DEVICE_DISCONNECTED = "Device Disconnected"
const val DEVICE_CONNECTED = "Device connected"
const val DEVICE_BATTERY_LOW = "Battery low"
const val DEVICE_ERROR = "Error on device"
const val DEVICE_WARNING = "Device warning"

fun isBluetoothEnabled(context: Context): Boolean {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return bluetoothManager.adapter.isEnabled
}

fun broadCastReceiver(onBlueToothStateChanged: (Int) -> Unit): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                onBlueToothStateChanged.invoke(state)
            }
        }
    }
}