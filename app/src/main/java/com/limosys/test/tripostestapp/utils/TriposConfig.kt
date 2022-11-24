package com.limosys.test.tripostestapp.utils

import com.vantiv.triposmobilesdk.ApplicationConfiguration
import com.vantiv.triposmobilesdk.BuildConfig
import com.vantiv.triposmobilesdk.Configuration
import com.vantiv.triposmobilesdk.DeviceConfiguration
import com.vantiv.triposmobilesdk.HostConfiguration
import com.vantiv.triposmobilesdk.TransactionConfiguration
import com.vantiv.triposmobilesdk.enums.AddressVerificationCondition
import com.vantiv.triposmobilesdk.enums.ApplicationMode
import com.vantiv.triposmobilesdk.enums.DeviceType
import com.vantiv.triposmobilesdk.enums.TerminalType
import com.vantiv.triposmobilesdk.express.Application
import com.vantiv.triposmobilesdk.express.Credentials
import com.vantiv.triposmobilesdk.utilities.BluetoothConfiguration
import java.math.BigDecimal

object TriposConfig {
    private lateinit var sharedConfig: Configuration
    private lateinit var credentials: Credentials
    private lateinit var app: Application
    private lateinit var deviceConfig: DeviceConfiguration
    private lateinit var transactionConfig: TransactionConfiguration


    fun getSharedConfig(): Configuration {
        this.sharedConfig = Configuration()
        setupApplicationConfiguration()
        setupHostConfiguration()
        setupDeviceConfiguration()
        setupTransactionConfiguration()
        return this.sharedConfig
    }


    private fun setupApplicationConfiguration() {
        val config = ApplicationConfiguration()
        config.idlePrompt = "TriPos Test"
        config.applicationMode = ApplicationMode.TestCertification
        this.sharedConfig.applicationConfiguration = config
    }

    private fun setupHostConfiguration() {
        val hostConfig = HostConfiguration()
        this.credentials = Credentials("1231758","130F69C952A6CCDB5CC9CB95F81136E599152B8FE76629B16F4583BFDF276B126A92D701","364801784")
        hostConfig.acceptorId = this.credentials.acceptorID
        hostConfig.accountId = this.credentials.accountID
        hostConfig.accountToken = this.credentials.accountToken

        this.app = Application(BuildConfig.LIBRARY_PACKAGE_NAME,"TriposTestApp","0.0")
        hostConfig.applicationId = this.app.applicationID
        hostConfig.applicationName = this.app.applicationName
        hostConfig.applicationVersion = this.app.applicationVersion

        this.sharedConfig.hostConfiguration = hostConfig
    }

    private fun setupDeviceConfiguration() {
        this.deviceConfig = DeviceConfiguration()
        this.deviceConfig.isContactlessAllowed = true
        this.deviceConfig.deviceType = DeviceType.BBPosDevice
        this.deviceConfig.isKeyedEntryAllowed = true
        this.deviceConfig.terminalId = "1234"
        this.deviceConfig.terminalType = TerminalType.Mobile

        val bluetoothConfiguration = BluetoothConfiguration()
        this.deviceConfig.bluetoothConfiguration = bluetoothConfiguration
        this.sharedConfig.deviceConfiguration = this.deviceConfig
    }
    private fun setupTransactionConfiguration() {
        this.transactionConfig = TransactionConfiguration()
        this.transactionConfig.addressVerificationCondition = AddressVerificationCondition.Keyed
        this.transactionConfig.isAmountConfirmationEnabled = true
        this.transactionConfig.isDebitAllowed = true
        this.transactionConfig.isCashbackAllowed = true
        this.transactionConfig.preReadQuickChipPlaceHolderAmount = BigDecimal.ONE;
        this.sharedConfig.transactionConfiguration = this.transactionConfig
    }
}
