package com.limosys.test.tripostestapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.limosys.test.tripostestapp.base.BaseActivity
import com.limosys.test.tripostestapp.ui.navigation.TriposNavigation
import com.limosys.test.tripostestapp.ui.theme.TriposTestAppTheme
import com.limosys.test.tripostestapp.utils.TriposConfig
import com.vantiv.triposmobilesdk.VTP
import com.vantiv.triposmobilesdk.triPOSMobileSDK
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalPermissionsApi::class)
@AndroidEntryPoint
class TriposTestMainActivity : BaseActivity() {
    private lateinit var sharedVtp: VTP
    override fun onCreate(savedInstanceState: Bundle?) {
        this.sharedVtp = triPOSMobileSDK.getSharedVtp()
        super.onCreate(savedInstanceState)
        setContent {
            AppEntryPoint()
        }
    }
}

@Composable
fun AppEntryPoint() {
    TriposTestAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            TriposNavigation()
        }
    }
}
