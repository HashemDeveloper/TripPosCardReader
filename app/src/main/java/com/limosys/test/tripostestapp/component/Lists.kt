package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.limosys.test.tripostestapp.component.styles.Spacing

@Composable
fun DisplayDebugList(detailList: MutableList<String>) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.TINY_8.space)) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopStart),content = {
            items(items = detailList) {value ->
                Text(text = value)
            }
        })
    }
}