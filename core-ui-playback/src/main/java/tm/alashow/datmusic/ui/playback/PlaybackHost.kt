/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.toast
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.PlaybackConnection

val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}

@OptIn(ExperimentalMaterialApi::class)
val LocalPlaybackSheetState = staticCompositionLocalOf<BottomSheetState> {
    error("No LocalPlaybackSheetState provided")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaybackHost(
    viewModel: PlaybackViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val playbackState by rememberFlowWithLifecycle(viewModel.playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    LaunchedEffect(playbackState.errorCode) {
        playbackState.errorMessage?.apply { context.toast("Playback error: ${playbackState.errorMessage}") }
    }

    val playbackBottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)

    CompositionLocalProvider(
        LocalPlaybackConnection provides viewModel.playbackConnection,
        LocalPlaybackSheetState provides playbackBottomSheetState,
    ) {
        PlaybackSheet {
            content()
        }
    }
}
