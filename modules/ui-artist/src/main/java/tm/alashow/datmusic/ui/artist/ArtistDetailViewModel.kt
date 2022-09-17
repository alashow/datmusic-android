/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.interactors.artist.GetArtistDetails
import tm.alashow.datmusic.data.observers.artist.ObserveArtist
import tm.alashow.datmusic.data.observers.artist.ObserveArtistDetails
import tm.alashow.navigation.screens.ARTIST_ID_KEY

@HiltViewModel
internal class ArtistDetailViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val artist: ObserveArtist,
    private val artistDetails: ObserveArtistDetails
) : ViewModel() {

    private val artistParams = DatmusicArtistParams(handle.get<String>(ARTIST_ID_KEY)!!)

    val state = combine(artist.flow, artistDetails.asyncFlow, ::ArtistDetailViewState)
        .stateInDefault(viewModelScope, ArtistDetailViewState.Empty)

    init {
        load()
    }

    private fun load(forceRefresh: Boolean = false) {
        artist(artistParams)
        artistDetails(GetArtistDetails.Params(artistParams, forceRefresh))
    }

    fun refresh() = load(true)
}
