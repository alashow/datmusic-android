/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Playlists
import tm.alashow.domain.models.Params

class ObservePlaylists @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<Params, Playlists>() {
    override fun createObservable(params: Params): Flow<Playlists> = playlistsRepo.playlists()
}
