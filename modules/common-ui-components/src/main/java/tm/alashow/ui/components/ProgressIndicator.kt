/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.ui.Delayed

object ProgressIndicatorDefaults {
    val sizeMedium = 32.dp to 2.dp
    val sizeSmall = 16.dp to 1.dp
    val size = 48.dp to 4.dp
}

@Composable
fun ProgressIndicatorSmall(modifier: Modifier = Modifier) =
    ProgressIndicator(modifier, ProgressIndicatorDefaults.sizeSmall.first, ProgressIndicatorDefaults.sizeSmall.second)

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier) =
    ProgressIndicator(modifier, ProgressIndicatorDefaults.sizeMedium.first, ProgressIndicatorDefaults.sizeMedium.second)

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = ProgressIndicatorDefaults.size.first,
    strokeWidth: Dp = ProgressIndicatorDefaults.size.second,
    color: Color = MaterialTheme.colors.secondary,
) {
    CircularProgressIndicator(modifier.size(size), color, strokeWidth)
}

fun LazyListScope.fullScreenLoading(delayMillis: Long = 100) {
    item {
        Delayed(delayMillis = delayMillis) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillParentMaxSize()
            ) {
                ProgressIndicator()
            }
        }
    }
}
