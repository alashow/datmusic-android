/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.datmusic.playback.PlaybackConnection

val LocalScaffoldState = staticCompositionLocalOf<ScaffoldState> { error("No LocalScaffoldState provided") }

val LocalAnalytics = staticCompositionLocalOf<FirebaseAnalytics> {
    error("No LocalAnalytics provided")
}

val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}
