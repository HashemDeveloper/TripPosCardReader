package com.limosys.test.tripostestapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.limosys.test.tripostestapp.base.BaseActivity
import com.limosys.test.tripostestapp.ui.theme.TriposTestAppTheme
import com.limosys.test.tripostestapp.utils.TriposConfig
import com.vantiv.triposmobilesdk.VTP
import com.vantiv.triposmobilesdk.triPOSMobileSDK
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriposTestMainActivity : BaseActivity() {
    private lateinit var sharedVtp: VTP

    override fun onCreate(savedInstanceState: Bundle?) {
        this.sharedVtp = triPOSMobileSDK.getSharedVtp()
        initializeSdk()
        super.onCreate(savedInstanceState)
        setContent {
            TriposTestAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    private fun initializeSdk() {
        if (!this.sharedVtp.isInitialized) {
            Thread {
                try {
                    this.sharedVtp.initialize(applicationContext, TriposConfig.getSharedConfig(), this)
                } catch (e: Exception) {
                    print(e.message)
                }
            }.start()
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TriposTestAppTheme {
        Greeting("Android")
    }
}