/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.extensions.orNA
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PLAYBACK_PROGRESS_INTERVAL
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artist
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.artworkUri
import tm.alashow.datmusic.playback.isBuffering
import tm.alashow.datmusic.playback.isPlayEnabled
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.playback.title
import tm.alashow.ui.Dismissable
import tm.alashow.ui.coloredRippleClickable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.translucentSurface

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaybackMiniControls(playbackConnection: PlaybackConnection = LocalPlaybackConnection.current) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)

    val visible = playbackState.state in listOf(STATE_PLAYING, STATE_BUFFERING, STATE_PAUSED)
    AnimatedVisibility(visible = visible, enter = slideInVertically({ it / 2 }), exit = slideOutVertically({ it / 2 })) {
        PlaybackMiniControls(
            playbackState = playbackState,
            nowPlaying = nowPlaying,
            onPlayPause = { playbackConnection.mediaController?.playPause() }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaybackMiniControls(
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    height: Dp = 60.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    playbackSheetState: BottomSheetState = LocalPlaybackSheetState.current,
) {
    val coroutine = rememberCoroutineScope()
    val expand = { coroutine.launch { playbackSheetState.expand() } }
    Dismissable(onDismiss = { playbackConnection.transportControls?.stop() }) {
        Surface(color = Color.Transparent, modifier = Modifier.clickable { expand() }) {
            Column {
                PlaybackProgress(playbackState)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier
                        .height(height)
                        .fillMaxWidth()
                        .translucentSurface()
                ) {
                    PlaybackNowPlaying(nowPlaying, height)
                    PlaybackPlayPause(onPlayPause, playbackState)
                }

                Box(Modifier.translucentSurface()) {
                    Divider(thickness = 0.2.dp)
                }
            }
        }
    }
}

@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val sizeModifier = Modifier
        .height(1.dp)
        .fillMaxWidth()
    val playbackProgress by rememberFlowWithLifecycle(playbackConnection.playbackProgress).collectAsState(0f)
    when {
        playbackState.isBuffering -> {
            LinearProgressIndicator(
                color = color,
                modifier = modifier.then(sizeModifier)
            )
        }
        else -> {
            LinearProgressIndicator(
                progress = animateFloatAsState(playbackProgress, tween(PLAYBACK_PROGRESS_INTERVAL.toInt(), easing = LinearEasing)).value,
                color = color,
                modifier = modifier.then(sizeModifier)
            )
        }
    }
}

@Composable
private fun RowScope.PlaybackNowPlaying(nowPlaying: MediaMetadataCompat, height: Dp) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier.weight(7f),
    ) {
        val artwork = rememberImagePainter(nowPlaying.artwork ?: nowPlaying.artworkUri, builder = ImageLoading.defaultConfig)
        CoverImage(painter = artwork, size = height, shape = RectangleShape) { imageMod ->
            Image(
                painter = artwork,
                contentDescription = null,
                modifier = imageMod
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = AppTheme.specs.paddingSmall)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                nowPlaying.title.orNA(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    nowPlaying.artist.orNA(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
private fun RowScope.PlaybackPlayPause(onPlayPause: () -> Unit, playbackState: PlaybackStateCompat) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.Companion
            .weight(1f)
            .coloredRippleClickable(onPlayPause),
    ) {
        Icon(
            painter = rememberVectorPainter(
                when {
                    playbackState.isPlaying -> Icons.Filled.Pause
                    playbackState.isPlayEnabled -> Icons.Filled.PlayArrow
                    else -> Icons.Filled.HourglassBottom
                }
            ),
            modifier = Modifier.size(32.dp),
            contentDescription = null
        )
    }
}
