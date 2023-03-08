package com.limosys.test.tripostestapp


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.limosys.test.tripostestapp.base.BaseActivity
import com.limosys.test.tripostestapp.ui.navigation.TriposNavigation
import com.limosys.test.tripostestapp.ui.theme.TriposTestAppTheme
import com.vantiv.triposmobilesdk.VTP
import com.vantiv.triposmobilesdk.triPOSMobileSDK
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriposTestMainActivity : BaseActivity() {
    private val sharedVtp: VTP = triPOSMobileSDK.getSharedVtp()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppEntryPoint(this.sharedVtp)
        }
    }
}

@Composable
fun AppEntryPoint(sharedVtp: VTP) {
    TriposTestAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            TriposNavigation(sharedVtp)
        }
    }
}
