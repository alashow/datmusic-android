/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId

class ObservePlaylistExistence @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Boolean>() {
    override fun createObservable(params: PlaylistId): Flow<Boolean> = playlistsRepo.has(params)
}
