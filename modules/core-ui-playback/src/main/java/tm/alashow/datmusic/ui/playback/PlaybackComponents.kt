/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.toAudio

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlaybackPager(
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    content: @Composable (Audio, Int, Modifier) -> Unit,
) {
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue).collectAsState(PlaybackQueue())

    val playbackCurrentIndex = playbackQueue.currentIndex
    var lastRequestedPage by remember(playbackQueue, nowPlaying) { mutableStateOf<Int?>(playbackCurrentIndex) }

    if (!playbackQueue.isValid) {
        content(nowPlaying.toAudio(), playbackCurrentIndex, Modifier)
        return
    }

    val pagerState = rememberPagerState(initialPage = playbackCurrentIndex)

    LaunchedEffect(playbackCurrentIndex, pagerState) {
        if (playbackCurrentIndex != pagerState.currentPage) {
            Timber.d("Syncing pager page to index: $playbackCurrentIndex")
            pagerState.scrollToPage(playbackCurrentIndex)
        }
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (lastRequestedPage != page) {
                lastRequestedPage = page
                playbackConnection.transportControls?.skipToQueueItem(page.toLong())
            }
        }
    }

    HorizontalPager(
        count = playbackQueue.ids.size,
        state = pagerState,
        modifier = modifier
    ) { page ->
        val currentAudio = playbackQueue.audios.getOrNull(page) ?: Audio()

        val pagerMod = Modifier.graphicsLayer {
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
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
