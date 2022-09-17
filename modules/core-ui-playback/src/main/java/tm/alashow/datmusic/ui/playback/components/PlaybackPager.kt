/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback.components

import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.toAudio
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun PlaybackPager(
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    pagerState: PagerState = rememberPagerState(),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    content: @Composable (Audio, Int, Modifier) -> Unit,
) {
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
    val playbackCurrentIndex = playbackQueue.currentIndex
    var lastRequestedPage by remember(playbackQueue, nowPlaying) {
        mutableStateOf<Int?>(
            playbackCurrentIndex
        )
    }

    if (!playbackQueue.isValid) {
        content(nowPlaying.toAudio(), playbackCurrentIndex, modifier)
        return
    }
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(playbackCurrentIndex)
    }
    LaunchedEffect(playbackCurrentIndex, pagerState) {
        if (playbackCurrentIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(playbackCurrentIndex)
        }
        snapshotFlow { pagerState.isScrollInProgress }
            .filter { !it }
            .map { pagerState.currentPage }
            .collectLatest { page ->
                if (lastRequestedPage != page) {
                    lastRequestedPage = page
                    playbackConnection.transportControls?.skipToQueueItem(page.toLong())
                }
            }
    }
    HorizontalPager(
        count = playbackQueue.size,
        modifier = modifier,
        state = pagerState,
        key = { playbackQueue.getOrNull(it) ?: it },
        verticalAlignment = verticalAlignment,
    ) { page ->
        val currentAudio = playbackQueue.getOrNull(page) ?: Audio()

        val pagerMod = Modifier.graphicsLayer {
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
            // TODO: report to upstream if can be reproduced in isolation
            if (pageOffset.isNaN()) {
                return@graphicsLayer
            }

            lerp(
                start = 0.85f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            ).also { scale ->
                scaleX = scale
                scaleY = scale
            }
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )
        }
        content(currentAudio, page, pagerMod)
    }
}
