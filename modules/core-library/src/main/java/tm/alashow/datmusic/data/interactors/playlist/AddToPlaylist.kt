/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.PlaylistId

class AddToPlaylist @Inject constructor(
    private val repo: PlaylistsRepo,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<AddToPlaylist.Params, List<PlaylistId>>() {

    data class Params(val playlistId: PlaylistId, var audioIds: AudioIds, val ignoreExisting: Boolean = false)

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        repo.addAudiosToPlaylist(
            playlistId = params.playlistId,
            audioIds = params.audioIds,
            ignoreExisting = params.ignoreExisting
        )
    }
}
