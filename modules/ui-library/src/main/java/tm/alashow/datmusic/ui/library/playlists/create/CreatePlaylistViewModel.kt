/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.getMutableStateFlow
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.asValidationError
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

@HiltViewModel
internal class CreatePlaylistViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val createPlaylist: CreatePlaylist,
    private val analytics: Analytics,
    private val navigator: Navigator,
) : ViewModel() {

    private val nameState = handle.getMutableStateFlow("name", viewModelScope, "")
    private val nameErrorState = MutableStateFlow<ValidationError?>(null)

    val state = combine(nameState, nameErrorState, ::CreatePlaylistViewState)
        .stateInDefault(viewModelScope, CreatePlaylistViewState.Empty)

    fun setPlaylistName(value: String) {
        nameState.value = value
        nameErrorState.value = null
    }

    fun createPlaylist() {
        analytics.event("playlists.create")
        val params = CreatePlaylist.Params(
            name = nameState.value.orBlank(),
            generateNameIfEmpty = true
        )
        viewModelScope.launch {
            createPlaylist(params)
                .catch { nameErrorState.value = it.asValidationError() }
                .collectLatest { newPlaylist ->
                    navigator.navigate(LeafScreen.PlaylistDetail.buildRoute(newPlaylist.id))
                }
        }
    }
}
