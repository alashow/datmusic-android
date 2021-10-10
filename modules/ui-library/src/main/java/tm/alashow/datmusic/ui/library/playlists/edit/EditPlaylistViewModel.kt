/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.datmusic.data.interactors.playlist.DeletePlaylist
import tm.alashow.datmusic.data.interactors.playlist.UpdatePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.asValidationError
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY

@HiltViewModel
class EditPlaylistViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val observePlaylist: ObservePlaylist,
    private val updatePlaylist: UpdatePlaylist,
    private val deletePlaylist: DeletePlaylist,
    private val navigator: Navigator
) : ViewModel() {

    private val playlistId = requireNotNull(handle.get<PlaylistId>(PLAYLIST_ID_KEY))

    private val nameState = handle.getStateFlow("playlist_name", viewModelScope, TextFieldValue())
    val name = nameState.filterNotNull()

    private val nameErrorState = MutableStateFlow<ValidationError?>(null)
    val nameError = nameErrorState.asSharedFlow()

    init {
        observePlaylist(playlistId)
        viewModelScope.launch {
            val playlist = observePlaylist.get()
            nameState.value = TextFieldValue(playlist.name)
        }
    }

    fun setPlaylistName(value: TextFieldValue) {
        nameState.value = value
        nameErrorState.value = null
    }

    fun save() {
        viewModelScope.launch {
            val playlist = observePlaylist.get().copy(name = nameState.value?.text.orBlank())
            updatePlaylist(playlist).catch {
                nameErrorState.value = it.asValidationError()
            }.collect {
                navigator.goBack()
            }
        }
    }

    fun deletePlaylistItem() = viewModelScope.launch {
        deletePlaylist(playlistId).collect {
            navigator.goBack()
        }
    }
}
