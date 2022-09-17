/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback.components

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.ui.AdaptiveColorResult
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.coloredRippleClickable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.plainSurfaceColor

@Composable
internal fun PlaybackArtwork(
    artwork: Uri,
    contentColor: Color,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    onClick: Callback? = null,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    CoverImage(
        data = artwork,
        shape = RectangleShape,
        backgroundColor = plainSurfaceColor(),
        contentColor = contentColor,
        bitmapPlaceholder = nowPlaying.artwork,
        modifier = Modifier
            .padding(horizontal = AppTheme.specs.paddingLarge)
            .then(modifier),
        imageModifier = Modifier.coloredRippleClickable(
            onClick = {
                if (onClick != null) onClick.invoke()
                else playbackConnection.mediaController?.playPause()
            },
            color = contentColor,
            rippleRadius = Dp.Unspecified,
        ),
    )
}

@Composable
fun nowPlayingArtworkAdaptiveColor(
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
): State<AdaptiveColorResult> {
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
    return adaptiveColor(nowPlaying.artwork, initial = MaterialTheme.colorScheme.background)
}
