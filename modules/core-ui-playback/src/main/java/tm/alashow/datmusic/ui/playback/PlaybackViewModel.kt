/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.SET_MEDIA_STATE

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection,
    private val handle: SavedStateHandle,
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { connected ->
                if (connected) {
                    playbackConnection.transportControls?.sendCustomAction(SET_MEDIA_STATE, null)
                }
            }
        }
    }
}
