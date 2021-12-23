/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import tm.alashow.base.util.extensions.Callback
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.playPause
import tm.alashow.ui.LocalAdaptiveColorResult
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ShuffleButton(
    visible: Boolean,
    playbackConnection: PlaybackConnection,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colors.primary,
    tint: Color = MaterialTheme.colors.secondary,
    onLongClickLabel: String? = null,
    onLongClick: Callback = { playbackConnection.mediaController?.playPause() },
    onDoubleClick: Callback = { playbackConnection.mediaController?.playPause() },
    onClick: Callback = {},
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
    playbackConnection: PlaybackConnection,
    modifier: Modifier = Modifier,
    tint: Color = LocalAdaptiveColorResult.current.contentColor,
    background: Color = LocalAdaptiveColorResult.current.color,
    onLongClickLabel: String? = null,
    onLongClick: Callback = { playbackConnection.mediaController?.playPause() },
    onDoubleClick: Callback = { playbackConnection.mediaController?.playPause() },
    onClick: Callback = {},
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
