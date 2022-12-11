package com.limosys.test.tripostestapp.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import com.vantiv.triposmobilesdk.*
import com.vantiv.triposmobilesdk.emv.EmvContactlessConfiguration
import com.vantiv.triposmobilesdk.enums.AddressVerificationCondition
import com.vantiv.triposmobilesdk.enums.ApplicationMode
import com.vantiv.triposmobilesdk.enums.DeviceType
import com.vantiv.triposmobilesdk.enums.TerminalType
import com.vantiv.triposmobilesdk.express.Application
import com.vantiv.triposmobilesdk.express.Credentials
import com.vantiv.triposmobilesdk.utilities.BluetoothConfiguration
import java.math.BigDecimal
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


object TriposConfig {
    private lateinit var sharedConfig: Configuration
    private lateinit var credentials: Credentials
    private lateinit var app: Application
    private lateinit var deviceConfig: DeviceConfiguration
    private lateinit var transactionConfig: TransactionConfiguration
    private lateinit var emvConfiguration: EmvConfiguration

    fun getSharedConfig(identifier: String): Configuration {
        this.sharedConfig = Configuration()
        setupApplicationConfiguration()
        setupHostConfiguration()
        setupDeviceConfiguration(identifier)
        setupTransactionConfiguration()
        return this.sharedConfig
    }


    private fun setupApplicationConfiguration() {
        val config = ApplicationConfiguration()
        config.idlePrompt = "TriposTestApp"
        config.applicationMode = ApplicationMode.Production
        this.sharedConfig.applicationConfiguration = config
    }

    private fun setupHostConfiguration() {
        val hostConfig = HostConfiguration()
        this.credentials = Credentials("1231758","130F69C952A6CCDB5CC9CB95F81136E599152B8FE76629B16F4583BFDF276B126A92D701","364801784")
        hostConfig.acceptorId = this.credentials.acceptorID
        hostConfig.accountId = this.credentials.accountID
        hostConfig.accountToken = this.credentials.accountToken

        this.app = Application("15018","TriposTestApp","0.0")
        hostConfig.applicationId = this.app.applicationID
        hostConfig.applicationName = this.app.applicationName
        hostConfig.applicationVersion = this.app.applicationVersion

        this.sharedConfig.hostConfiguration = hostConfig
    }

    private fun setupDeviceConfiguration(identifier: String) {
        this.deviceConfig = DeviceConfiguration()
        this.deviceConfig.isContactlessAllowed = true
        this.deviceConfig.deviceType = DeviceType.BBPosDevice
        this.deviceConfig.isKeyedEntryAllowed = true

        this.deviceConfig.terminalId = "1234"
        this.deviceConfig.terminalType = TerminalType.Mobile

        val bluetoothConfiguration = BluetoothConfiguration()
        bluetoothConfiguration.identifier = identifier
        this.deviceConfig.bluetoothConfiguration = bluetoothConfiguration

        // TCP/IP configuration
        val tcpIpConfiguration = TcpIpConfiguration()
        tcpIpConfiguration.ipAddress = getIPAddress(true)
        tcpIpConfiguration.port = 12000
        deviceConfig.tcpIpConfiguration = tcpIpConfiguration

        this.sharedConfig.deviceConfiguration = this.deviceConfig
    }
    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr: String = addr.hostAddress as String
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%')
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(
                                    Locale.getDefault()
                                )
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {}
        return ""
    }
    fun getWifiIPAddress(context: Context): String? {
        val wifiMgr = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
        val wifiInfo = wifiMgr!!.connectionInfo
        val ip = wifiInfo.ipAddress
        return android.text.format.Formatter.formatIpAddress(ip)
    }
    private fun setupTransactionConfiguration() {
        this.transactionConfig = TransactionConfiguration()
        this.transactionConfig.addressVerificationCondition = AddressVerificationCondition.Keyed
        this.transactionConfig.isAmountConfirmationEnabled = true
        this.transactionConfig.isDebitAllowed = true
        this.transactionConfig.isEmvAllowed = true
        this.transactionConfig.isQuickChipAllowed = true
        this.transactionConfig.isCashbackAllowed = true
        this.transactionConfig.preReadQuickChipPlaceHolderAmount = BigDecimal.ONE;
        this.sharedConfig.transactionConfiguration = this.transactionConfig
    }

    private fun setupEMVConfiguration() {
        this.emvConfiguration = EmvConfiguration()
        this.emvConfiguration.isAutoSelectApplicationEnabled = true
        this.sharedConfig.emvConfiguration = this.emvConfiguration
    }
}
