/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.asValidationError
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val createPlaylist: CreatePlaylist,
    private val analytics: FirebaseAnalytics,
    private val navigator: Navigator,
) : ViewModel() {

    private val nameState = MutableStateFlow(TextFieldValue())
    val name = nameState.asStateFlow()

    private val nameErrorState = MutableStateFlow<ValidationError?>(null)
    val nameError = nameErrorState.asStateFlow()

    fun setPlaylistName(value: TextFieldValue) {
        nameState.value = value
        nameErrorState.value = null
    }

    fun createPlaylist() {
        analytics.event("playlists.create")
        val params = CreatePlaylist.Params(
            name = nameState.value.text.orBlank(),
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
