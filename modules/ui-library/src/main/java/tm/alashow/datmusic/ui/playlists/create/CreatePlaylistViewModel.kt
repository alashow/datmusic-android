/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playlists.create

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import tm.alashow.base.util.ValidationError
import tm.alashow.base.util.ValidationErrorBlank
import tm.alashow.base.util.ValidationErrorTooLong
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.navigation.Navigator

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlistsRepo: PlaylistsRepo,
    private val navigator: Navigator
) : ViewModel() {

    private val nameState = handle.getStateFlow("playlist_name", viewModelScope, TextFieldValue())
    val name = nameState.filterNotNull()

    private val nameErrorState = MutableStateFlow<ValidationError?>(null)
    val nameError = nameErrorState.asSharedFlow()

    fun setPlaylistName(value: TextFieldValue) {
        nameState.value = value
        nameErrorState.value = null
    }

    fun createPlaylist() {
        val name = nameState.value?.text ?: ""

        if (name.isBlank()) {
            nameErrorState.value = ValidationErrorBlank()
            return
        }
        if (name.length > PLAYLIST_NAME_MAX_LENGTH) {
            nameErrorState.value = ValidationErrorTooLong()
            return
        }

        viewModelScope.launch {
            playlistsRepo.clear()
            playlistsRepo.createPlaylist(Playlist(name = name))
            navigator.goBack()
        }
    }
}
