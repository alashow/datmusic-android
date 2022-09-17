/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import tm.alashow.common.compose.previews.CombinedPreview
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
import tm.alashow.datmusic.ui.playback.components.nowPlayingArtworkAdaptiveColor
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.ui.Dismissable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Theme

object PlaybackMiniControlsDefaults {
    val Height = 56.dp
}

@Composable
fun PlaybackMiniControls(
    modifier: Modifier = Modifier,
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
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaybackMiniControls(
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.Height,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    navigator: Navigator = LocalNavigator.current,
) {
    val openPlaybackSheet = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) }
    val adaptiveColor by nowPlayingArtworkAdaptiveColor()
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
                    onDoubleClick = onPlayPause,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
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
                var aspectRatio by remember { mutableStateOf(0f) }
                var controlsVisible by remember { mutableStateOf(true) }
                var nowPlayingVisible by remember { mutableStateOf(true) }
                var controlsEndPadding by remember { mutableStateOf(0.dp) }
                val controlsEndPaddingAnimated by animateDpAsState(controlsEndPadding)
                val smallPadding = Theme.specs.paddingSmall
                val tinyPadding = Theme.specs.paddingTiny

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .onGloballyPositioned {
                            aspectRatio = it.size.height.toFloat() / it.size.width.toFloat()
                            controlsVisible = aspectRatio < 0.9
                            nowPlayingVisible = aspectRatio < 0.5
                            controlsEndPadding = when (aspectRatio) {
                                in 0.0..0.15 -> 0.dp
                                in 0.15..0.35 -> tinyPadding
                                else -> smallPadding
                            }
                        }
                        .padding(if (controlsVisible) PaddingValues(end = controlsEndPaddingAnimated) else PaddingValues())
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        PlaybackNowPlaying(nowPlaying = nowPlaying, maxHeight = height, coverOnly = !nowPlayingVisible)
                        if (controlsVisible)
                            PlaybackPlayPause(playbackState = playbackState, onPlayPause = onPlayPause)
                    }
                }
                PlaybackProgress(
                    playbackState = playbackState,
                    color = MaterialTheme.colorScheme.onBackground
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
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        ProvideContentAlpha(ContentAlpha.medium) {
            Text(
                audio.artist.orNA(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RowScope.PlaybackPlayPause(
    playbackState: PlaybackStateCompat,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = AppTheme.specs.iconSize,
) {
    IconButton(
        onClick = onPlayPause,
        rippleColor = LocalContentColor.current,
        modifier = modifier.weight(1f)
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
    modifier: Modifier = Modifier,
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
                modifier = sizeModifier.then(modifier)
            )
        }
        else -> {
            val progress by animatePlaybackProgress(progressState.progress)
            LinearProgressIndicator(
                progress = progress,
                color = color,
                trackColor = color.copy(alpha = 0.24f),
                modifier = sizeModifier.then(modifier)
            )
        }
    }
}

@CombinedPreview
@Composable
fun PlaybackMiniControlsPreview() = PreviewDatmusicCore {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Theme.specs.padding)
    ) {
        PlaybackMiniControls(Modifier.widthIn(max = 400.dp))
        PlaybackMiniControls(Modifier.width(200.dp))
        PlaybackMiniControls(Modifier.width(120.dp))
        PlaybackMiniControls(Modifier.width(72.dp))
    }
}
