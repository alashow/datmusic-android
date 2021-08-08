/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OffsetNotifyingBox(headerHeight: Dp = 300.dp, content: @Composable (offsetPx: State<Float>, progress: State<Float>) -> Unit) {
    val headerHeightPx = with(LocalDensity.current) { headerHeight.roundToPx().toFloat() }
    val headerOffsetHeightPx = remember { mutableStateOf(0f) }
    val progress = derivedStateOf { (-headerOffsetHeightPx.value / headerHeightPx).coerceIn(0.0f, 1.0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = headerOffsetHeightPx.value + delta
                headerOffsetHeightPx.value = newOffset.coerceIn(-headerHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Box(Modifier.nestedScroll(nestedScrollConnection)) {
        content(headerOffsetHeightPx, progress)
    }
}
