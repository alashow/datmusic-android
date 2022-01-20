/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import tm.alashow.base.util.extensions.orNA
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.artworkUri
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.playback.isBuffering
import tm.alashow.datmusic.playback.isError
import tm.alashow.datmusic.playback.isPlayEnabled
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.ui.playback.components.PlaybackPager
import tm.alashow.datmusic.ui.playback.components.animatePlaybackProgress
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.ui.Dismissable
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.theme.AppTheme

object PlaybackMiniControlsDefaults {
    val height = 56.dp
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaybackMiniControls(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)

    val visible = (playbackState to nowPlaying).isActive
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }),
        exit = slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        PlaybackMiniControls(
            playbackState = playbackState,
            nowPlaying = nowPlaying,
            onPlayPause = { playbackConnection.mediaController?.playPause() },
            contentPadding = contentPadding,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlaybackMiniControls(
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    height: Dp = PlaybackMiniControlsDefaults.height,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    navigator: Navigator = LocalNavigator.current,
) {
    val openPlaybackSheet = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) }
    val adaptiveColor by adaptiveColor(nowPlaying.artwork, initial = MaterialTheme.colors.background)
    val backgroundColor = adaptiveColor.color
    val contentColor = adaptiveColor.contentColor

    Dismissable(onDismiss = { playbackConnection.transportControls?.stop() }) {
        var dragOffset by remember { mutableStateOf(0f) }
        Surface(
            color = Color.Transparent,
            shape = MaterialTheme.shapes.small,
            modifier = modifier
                .padding(horizontal = AppTheme.specs.paddingSmall)
                .animateContentSize()
                .combinedClickable(
                    enabled = true,
                    onClick = openPlaybackSheet,
                    onLongClick = onPlayPause,
                    onDoubleClick = onPlayPause
                )
                // open playback sheet on swipe up
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState(
                        onDelta = {
                            dragOffset = it.coerceAtMost(0f)
                        }
                    ),
                    onDragStarted = {
                        if (dragOffset < 0) openPlaybackSheet()
                    },
                )
        ) {
            Column {
                var controlsVisible by remember { mutableStateOf(true) }
                var nowPlayingVisible by remember { mutableStateOf(true) }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .onGloballyPositioned {
                            val aspectRatio = it.size.height.toFloat() / it.size.width.toFloat()
                            controlsVisible = aspectRatio < 0.9
                            nowPlayingVisible = aspectRatio < 0.5
                        }
                        .padding(if (controlsVisible) contentPadding else PaddingValues())
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        PlaybackNowPlaying(nowPlaying = nowPlaying, maxHeight = height, coverOnly = !nowPlayingVisible)
                        if (controlsVisible)
                            PlaybackPlayPause(playbackState = playbackState, onPlayPause = onPlayPause)
                    }
                }
                PlaybackProgress(
                    playbackState = playbackState,
                    color = MaterialTheme.colors.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun RowScope.PlaybackNowPlaying(
    nowPlaying: MediaMetadataCompat,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    coverOnly: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.weight(if (coverOnly) 3f else 7f),
    ) {
        CoverImage(
            data = nowPlaying.artwork ?: nowPlaying.artworkUri,
            size = maxHeight - AppTheme.specs.padding,
            modifier = Modifier.padding(AppTheme.specs.paddingSmall)
        )

        if (!coverOnly)
            PlaybackPager(nowPlaying = nowPlaying) { audio, _, pagerMod ->
                PlaybackNowPlaying(audio, modifier = pagerMod)
            }
    }
}

@Composable
private fun PlaybackNowPlaying(audio: Audio, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .padding(vertical = AppTheme.specs.paddingSmall)
            .fillMaxWidth()
            .then(modifier)
    ) {
        Text(
            audio.title.orNA(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                audio.artist.orNA(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun RowScope.PlaybackPlayPause(
    playbackState: PlaybackStateCompat,
    size: Dp = AppTheme.specs.iconSize,
    onPlayPause: () -> Unit
) {
    IconButton(
        onClick = onPlayPause,
        rippleColor = LocalContentColor.current,
        modifier = Modifier.weight(1f)
    ) {
        Icon(
            imageVector = when {
                playbackState.isError -> Icons.Filled.ErrorOutline
                playbackState.isPlaying -> Icons.Filled.Pause
                playbackState.isPlayEnabled -> Icons.Filled.PlayArrow
                else -> Icons.Filled.HourglassBottom
            },
            modifier = Modifier.size(size),
            contentDescription = null
        )
    }
}

@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    color: Color,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress)
    val sizeModifier = Modifier
        .height(2.dp)
        .fillMaxWidth()
    when {
        playbackState.isBuffering -> {
            LinearProgressIndicator(
                color = color,
                modifier = sizeModifier
            )
        }
        else -> {
            val progress by animatePlaybackProgress(progressState.progress)
            LinearProgressIndicator(
                progress = progress,
                color = color,
                backgroundColor = color.copy(ProgressIndicatorDefaults.IndicatorBackgroundOpacity),
                modifier = sizeModifier
            )
        }
    }
}
