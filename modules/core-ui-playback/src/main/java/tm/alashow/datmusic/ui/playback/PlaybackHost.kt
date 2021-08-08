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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler

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
    val playbackBottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)

    CompositionLocalProvider(
        LocalPlaybackConnection provides viewModel.playbackConnection,
        LocalPlaybackSheetState provides playbackBottomSheetState,
    ) {
        PlaybackSheet(
            sheetContentWrapper = { sheetContent ->
                val audioActionHandler = audioActionHandler()
                CompositionLocalProvider(LocalAudioActionHandler provides audioActionHandler) {
                    sheetContent()
                }
            },
            content = content
        )
    }
}
