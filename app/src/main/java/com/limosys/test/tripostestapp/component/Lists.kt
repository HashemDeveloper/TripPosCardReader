package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.limosys.test.tripostestapp.component.styles.Spacing
import kotlinx.coroutines.launch

@Composable
fun DisplayDebugList(detailList: MutableList<String>) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.TINY_8.space)) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, color = Color.Gray, RoundedCornerShape(8.dp))
            .height(300.dp)
            .padding(8.dp)
            .align(Alignment.TopStart), state = listState ,content = {
            itemsIndexed(items = detailList) {index, value ->
                Text(text = value)
                if (index == detailList.size-1) {
                   LaunchedEffect(key1 = index, block = {
                       coroutineScope.launch {
                           listState.animateScrollToItem(index)
                       }
                   })
                }
            }
        })
    }
}