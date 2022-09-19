/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme

@Composable
fun coverHeaderScrollProgress(listState: LazyListState, height: Dp = CoverHeaderDefaults.height): State<Float> {
    val density = LocalDensity.current.density
    val headerProgress = remember { mutableStateOf(0f) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { (listState.firstVisibleItemScrollOffset / density).dp / height }
            .map { if (listState.firstVisibleItemIndex == 0) it else 1f }
            .map { it.coerceIn(0f, 1f) }
            .distinctUntilChanged()
            .collectLatest { headerProgress.value = it }
    }
    return headerProgress
}

class MediaDetailHeader {
    operator fun invoke(
        list: LazyListScope,
        listState: LazyListState,
        headerBackgroundMod: Modifier,
        title: String,
        artwork: Any?,
        onTitleClick: () -> Unit,
        headerCoverIcon: VectorPainter?,
        extraHeaderContent: (@Composable (ColumnScope.() -> Unit))?,
        modifier: Modifier = Modifier,
    ) {
        list.item {
            val headerOffsetProgress = coverHeaderScrollProgress(listState)

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = headerBackgroundMod
                    .then(modifier)
                    .padding(AppTheme.specs.padding)
                    .statusBarsPadding()
            ) {
                CoverHeaderRow(
                    title = title,
                    imageData = artwork,
                    icon = headerCoverIcon,
                    offsetProgress = headerOffsetProgress,
                    titleModifier = Modifier.simpleClickable(onClick = onTitleClick),
                )
                extraHeaderContent?.invoke(this)
            }
        }
    }
}
