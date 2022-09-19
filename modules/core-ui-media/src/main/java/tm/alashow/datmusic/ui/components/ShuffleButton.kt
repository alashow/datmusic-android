/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.LocalAdaptiveColor

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ShuffleButton(
    visible: Boolean,
    playbackConnection: PlaybackConnection,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.primary,
    tint: Color = MaterialTheme.colorScheme.secondary,
    onLongClickLabel: String? = null,
    onLongClick: () -> Unit = { playbackConnection.mediaController?.playPause() },
    onDoubleClick: () -> Unit = { playbackConnection.mediaController?.playPause() },
    onClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        Box(modifier) {
            IconButton(
                onClick = onClick,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(background)
                    .size(AppTheme.specs.iconSizeLarge)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Shuffle),
                    tint = tint,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ShuffleAdaptiveButton(
    visible: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalAdaptiveColor.current.contentColor,
    background: Color = LocalAdaptiveColor.current.color,
    onLongClickLabel: String? = null,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    onLongClick: () -> Unit = { playbackConnection.mediaController?.playPause() },
    onDoubleClick: () -> Unit = { playbackConnection.mediaController?.playPause() },
    onClick: () -> Unit = {},
) {
    ShuffleButton(
        visible = visible,
        playbackConnection = playbackConnection,
        modifier = modifier,
        background = background,
        tint = tint,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick,
    )
}
