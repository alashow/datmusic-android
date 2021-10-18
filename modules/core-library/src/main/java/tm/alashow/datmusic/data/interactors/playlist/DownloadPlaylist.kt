/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.billing.Subscriptions
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.AsyncInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEventsError

class DownloadPlaylist @Inject constructor(
    private val repo: PlaylistsRepo,
    private val downloader: Downloader,
    private val dispatchers: CoroutineDispatchers,
) : AsyncInteractor<PlaylistId, Int>() {

    override suspend fun prepare(params: PlaylistId) {
        downloader.clearDownloaderEvents()
        Subscriptions.checkPremiumPermission()
    }

    override suspend fun doWork(params: PlaylistId) = withContext(dispatchers.io) {
        val audios = repo.playlistAudios(params).first().map { it.audio }
        var enqueuedCount = 0

        audios.forEach {
            if (downloader.enqueueAudio(it))
                enqueuedCount++
        }

        val events = downloader.downloaderEventsAll
        if (enqueuedCount == 0 && events.isNotEmpty())
            throw DownloaderEventsError(downloader.downloaderEventsAll)

        return@withContext enqueuedCount
    }
}
