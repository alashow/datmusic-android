/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.runtime.staticCompositionLocalOf
import tm.alashow.datmusic.playback.PlaybackConnection

// TODO: Move somewhere else
val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}
