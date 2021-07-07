/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ProgressIndicatorDefaults {
    val sizeMedium = 32.dp to 3.dp
    val sizeSmall = 16.dp to 2.dp
    val size = 48.dp to 4.dp
}

@Composable
fun ProgressIndicatorSmall(modifier: Modifier = Modifier) =
    ProgressIndicator(ProgressIndicatorDefaults.sizeSmall.first, ProgressIndicatorDefaults.sizeSmall.second, modifier)

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier) =
    ProgressIndicator(ProgressIndicatorDefaults.sizeMedium.first, ProgressIndicatorDefaults.sizeMedium.second, modifier)

@Composable
fun ProgressIndicator(
    size: Dp = ProgressIndicatorDefaults.size.first,
    strokeWidth: Dp = ProgressIndicatorDefaults.size.second,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
) {
    CircularProgressIndicator(modifier.size(size), color, strokeWidth)
}
