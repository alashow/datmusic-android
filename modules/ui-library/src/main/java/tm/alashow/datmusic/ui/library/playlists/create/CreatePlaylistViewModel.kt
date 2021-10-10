/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

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
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.asValidationError
import tm.alashow.navigation.Navigator

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val createPlaylist: CreatePlaylist,
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
        val params = CreatePlaylist.Params(
            name = nameState.value?.text ?: "",
            generateNameIfEmpty = true
        )
        viewModelScope.launch {
            createPlaylist(params).catch {
                nameErrorState.value = it.asValidationError()
            }.collect {
                navigator.goBack()
            }
        }
    }
}
