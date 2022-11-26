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
//            TriposTestAppTheme {
//                MainContent()
//            }
            AppEntryPoint()
        }
    }

    @Composable
    fun MainContent() {
        val permissionsToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rememberMultiplePermissionsState(
                permissions = listOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            rememberMultiplePermissionsState(
                permissions = listOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                InitializeSdkButton(permissionsToCheck)
            }
        }
    }

    @Composable
    private fun InitializeSdkButton(permissionsToCheck: MultiplePermissionsState) {
        val context: Context = LocalContext.current
        if (permissionsToCheck.allPermissionsGranted) {
            initializeSdk {
                runOnUiThread {
                    Toast.makeText(
                        context,
                        "Initialized",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            Text(text = "Please connect bluetooth device once initialization is successful.",
            textAlign = TextAlign.Center)
        } else {
            getTextToShowGivenPermissions(
                permissionsToCheck.revokedPermissions, permissionsToCheck.shouldShowRationale
            )
            Button(onClick = {
                permissionsToCheck.launchMultiplePermissionRequest()
            }) {
                Text(text = "Request Permission")
            }
        }
    }

    private fun initializeSdk(view: () -> Unit) {
        if (!this.sharedVtp.isInitialized) {
            Thread {
                try {
                    this.sharedVtp.initialize(
                        applicationContext, TriposConfig.getSharedConfig(), this
                    )
                    view.invoke()
                } catch (e: Exception) {
                    print(e.message)
                }
            }.start()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun getTextToShowGivenPermissions(
        permissions: List<PermissionState>, shouldShowRationale: Boolean
    ): String {
        val revokedPermissionsSize = permissions.size
        if (revokedPermissionsSize == 0) return ""

        val textToShow = StringBuilder().apply {
            append("The ")
        }

        for (i in permissions.indices) {
            textToShow.append(permissions[i].permission)
            when {
                revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                    textToShow.append(", and ")
                }
                i == revokedPermissionsSize - 1 -> {
                    textToShow.append(" ")
                }
                else -> {
                    textToShow.append(", ")
                }
            }
        }
        textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
        textToShow.append(
            if (shouldShowRationale) {
                " important. Please grant all of them for the app to function properly."
            } else {
                " denied. The app cannot function without them."
            }
        )
        return textToShow.toString()
    }

    @Composable
    @Preview(showSystemUi = true)
    fun DefaultPreview() {
        MainContent()
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
