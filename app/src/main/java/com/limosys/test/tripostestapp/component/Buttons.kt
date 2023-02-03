package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.limosys.test.tripostestapp.component.styles.Spacing

@Composable
fun DebugButton(onDebugClicked: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.SMALL_16_DP.space)) {
        Button(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter) , onClick = {
            onDebugClicked.invoke()
        }) {
            Text(text = "Debug")
        }
    }
}