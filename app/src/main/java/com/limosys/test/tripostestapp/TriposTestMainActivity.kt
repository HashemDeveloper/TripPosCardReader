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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriposTestMainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
