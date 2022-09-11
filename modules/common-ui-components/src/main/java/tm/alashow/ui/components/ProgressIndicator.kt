/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.ui.Delayed
import tm.alashow.ui.scaffoldPadding

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
    color: Color = MaterialTheme.colorScheme.secondary,
) {
    CircularProgressIndicator(modifier.size(size), color, strokeWidth)
}

private const val FULL_SCREEN_LOADING_DELAY = 100L

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier, delayMillis: Long = FULL_SCREEN_LOADING_DELAY) {
    Delayed(delayMillis = delayMillis) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = when (modifier == Modifier) {
                true -> Modifier.fillMaxSize().padding(scaffoldPadding())
                false -> modifier
            }
        ) {
            ProgressIndicator()
        }
    }
}

fun LazyListScope.fullScreenLoading(delayMillis: Long = 100) {
    item {
        FullScreenLoading(delayMillis = delayMillis, modifier = Modifier.fillParentMaxHeight())
    }
}
