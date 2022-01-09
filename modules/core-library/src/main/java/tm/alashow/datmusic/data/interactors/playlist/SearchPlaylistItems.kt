/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.db.daos.AudiosFtsDao
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItem

class SearchPlaylistItems @Inject constructor(
    private val audiosFtsDao: AudiosFtsDao,
) : SubjectInteractor<SearchPlaylistItems.Params, List<PlaylistItem>>() {

    data class Params(val playlistId: PlaylistId, val query: String)

    override fun createObservable(params: Params): Flow<List<PlaylistItem>> {
        return audiosFtsDao.searchPlaylist(params.playlistId, "*${params.query}*")
    }
}
