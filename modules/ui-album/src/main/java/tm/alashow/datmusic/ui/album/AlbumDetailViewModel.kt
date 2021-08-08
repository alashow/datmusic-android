/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import tm.alashow.datmusic.data.interactors.GetAlbumDetails
import tm.alashow.datmusic.data.observers.ObserveAlbum
import tm.alashow.datmusic.data.observers.ObserveAlbumDetails
import tm.alashow.datmusic.data.repos.album.DatmusicAlbumParams
import tm.alashow.navigation.ALBUM_ACCESS_KEY
import tm.alashow.navigation.ALBUM_ID_KEY
import tm.alashow.navigation.ALBUM_OWNER_ID_KEY

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val albumObserver: ObserveAlbum,
    private val albumDetails: ObserveAlbumDetails
) : ViewModel() {

    private val albumParams = DatmusicAlbumParams(
        requireNotNull(handle.get<Long>(ALBUM_ID_KEY)),
        requireNotNull(handle.get<Long>(ALBUM_OWNER_ID_KEY)),
        requireNotNull(handle.get<String>(ALBUM_ACCESS_KEY))
    )

    val state = combine(albumObserver.observe(), albumDetails.observe(), ::AlbumDetailViewState).shareIn(viewModelScope, SharingStarted.Lazily, 1)

    init {
        load()
    }

    private fun load(forceRefresh: Boolean = false) = viewModelScope.launch {
        albumObserver(albumParams)
        albumDetails(GetAlbumDetails.Params(albumParams, forceRefresh))
    }

    fun refresh() = load(true)
}
