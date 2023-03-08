package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.limosys.test.tripostestapp.component.styles.Spacing

@Composable
fun TriposSingleButtonDialog(
    modifier: Modifier,
    actionButtonModifier: Modifier = Modifier,
    title: String,
    actionButtonText: String,
    content: @Composable () -> Unit,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    onDismissRequest: () -> Unit,
    onCancelClicked: () -> Unit) {
    AlertDialog(
        title = { Text(
            modifier = modifier,
            text = title,
            style = MaterialTheme.typography.body1
        ) },
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        text = content,
        buttons = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = actionButtonModifier
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
fun TriposDoubleButtonDialog(
    modifier: Modifier,
    positiveButtonModifier: Modifier,
    negativeButtonModifier: Modifier,
    title: String,
    positiveButtonText: String,
    negativeButtonText: String,
    content: @Composable () -> Unit,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    onDismissRequest: () -> Unit,
    onPositiveAction: () -> Unit,
    onNegativeAction: () -> Unit) {
    AlertDialog(
        title = { Text(
            modifier = modifier,
            text = title,
            style = MaterialTheme.typography.body1
        ) },
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        text = content,
        buttons = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.TINY_4.space),
                horizontalArrangement = Arrangement.Start) {
                Button(
                    modifier = positiveButtonModifier,
                    onClick = {
                        onPositiveAction.invoke()
                    }) {
                    Text(text = positiveButtonText)
                }
                Button(
                    modifier = negativeButtonModifier,
                    onClick = {
                        onNegativeAction.invoke()
                    }) {
                    Text(text = negativeButtonText)
                }
            }
        },
        properties = DialogProperties(dismissOnBackPress, dismissOnClickOutside)
    )
}

@Composable
@Preview
fun Preview_Dialogs() {
    TriposDoubleButtonDialog(
        modifier = Modifier,
        positiveButtonModifier = Modifier.padding(start = Spacing.SMALL_16_DP.space),
        negativeButtonModifier = Modifier.padding(start = Spacing.SMALL_12_DP.space),
        title = "Are you sure?",
        positiveButtonText = "Yes",
        negativeButtonText = "No",
        content = {
                  Text(text = "If you do this your life will be doomed!")
        },
        onDismissRequest = {}, onPositiveAction = {}) {}
}