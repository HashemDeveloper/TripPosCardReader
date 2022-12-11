package com.limosys.test.tripostestapp.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.limosys.test.tripostestapp.component.styles.Opacity
import com.limosys.test.tripostestapp.component.styles.Spacing

@Composable
fun StandardDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = Opacity.OUTLINE.opacity),
    thickness: Dp = Spacing.TINY_1.space,
    startIndent: Dp = Spacing.NONE.space
) {
    Divider(
        modifier = modifier.padding(top = Spacing.TINY_3.space, bottom = Spacing.TINY_4.space),
        color = color,
        thickness = thickness,
        startIndent = startIndent
    )
}

@Preview
@Composable
private fun DefaultPreview() {
    StandardDivider()
}