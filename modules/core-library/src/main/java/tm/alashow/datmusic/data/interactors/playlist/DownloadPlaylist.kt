/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.downloader.Downloader

class DownloadPlaylist @Inject constructor(
    private val repo: PlaylistsRepo,
    private val downloader: Downloader,
    private val dispatchers: CoroutineDispatchers,
) : ResultInteractor<PlaylistId, Boolean>() {

    override suspend fun doWork(params: PlaylistId) = withContext(dispatchers.io) {
        val audios = repo.playlistAudios(params).first().map { it.audio }
        audios.forEach {
            downloader.enqueueAudio(it)
        }
        return@withContext true
    }
}
