/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalPlaybackConnection

@Composable
fun PlaybackHost(
    viewModel: PlaybackConnectionViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPlaybackConnection provides viewModel.playbackConnection,
    ) {
        content()
    }
}
