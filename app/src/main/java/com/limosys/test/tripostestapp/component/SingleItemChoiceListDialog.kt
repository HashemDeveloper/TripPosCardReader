package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.limosys.test.tripostestapp.component.styles.Spacing

@Composable
fun SingleItemChoiceListDialog(
    modifier: Modifier = Modifier,
    title: String,
    items: MutableList<String>,
    actionButtonText: String,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    onItemSelected: (String) -> Unit,
    onCancelClicked: () -> Unit) {
    AlertDialog(
        title = { Text(
            modifier = Modifier.padding(bottom = Spacing.TINY_4.space),
            text = title,
            style = MaterialTheme.typography.body1
        ) },
        onDismissRequest = {
            onCancelClicked.invoke()
        },
        text = {
            ChoiceList(modifier = modifier, items = items) {
                onItemSelected.invoke(it)
            }
        },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = Spacing.SMALL_16_DP.space, bottom = Spacing.SMALL_16_DP.space),
                    onClick = {
                        onCancelClicked.invoke()
                    }) {
                    Text(text = actionButtonText)
                }
            }
        },
        properties = DialogProperties(dismissOnBackPress, dismissOnClickOutside)
    )
}

@Composable
private fun ChoiceList(
    modifier: Modifier, items: MutableList<String>,
    onItemSelected: (String) -> Unit) {
    var selectedItem by rememberSaveable {
        mutableStateOf(items[0])
    }
    LazyColumn(modifier = modifier, content = {
        itemsIndexed(items = items) {index, item ->
            SingleChoiceView(item = item, selectedItem = selectedItem) {
                selectedItem = it
                onItemSelected.invoke(selectedItem)
            }
        }
    })
}

@Composable
fun SingleChoiceView(item: String, selectedItem: String, onItemSelected: (String) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.TINY_4.space),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = item == selectedItem,
            onClick = {
                onItemSelected.invoke(item)
            })
        Text(
            modifier = Modifier.padding(bottom = Spacing.TINY_1.space),
            text = item,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    SingleItemChoiceListDialog(
        title = "Please choose a device",
        items = TEST_LIST.toMutableList(),
        actionButtonText = "Cancel",
        onItemSelected = {}
    ) {}
}
private val TEST_LIST = listOf("DeviceOne","DeviceTwo","DeviceThree")