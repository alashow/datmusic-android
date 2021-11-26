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
import tm.alashow.datmusic.coreLibrary.R
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEventsError
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.ValidationError

object PlaylistIsEmpty : ValidationError(UiMessage.Resource(R.string.playlist_download_error_empty))

class DownloadPlaylist @Inject constructor(
    private val repo: PlaylistsRepo,
    private val downloader: Downloader,
    private val dispatchers: CoroutineDispatchers,
) : AsyncInteractor<PlaylistId, Int>() {

    override suspend fun prepare(params: PlaylistId) {
        downloader.clearDownloaderEvents()
        Subscriptions.checkPremiumPermission()
        if (repo.playlistItems(params).first().isEmpty())
            throw PlaylistIsEmpty.error()
    }

    override suspend fun doWork(params: PlaylistId) = withContext(dispatchers.io) {
        val audios = repo.playlistItems(params).first().map { it.audio }
        var enqueuedCount = 0

        audios.forEach {
            if (downloader.enqueueAudio(it))
                enqueuedCount++
        }

        val events = downloader.getDownloaderEvents()
        if (enqueuedCount == 0 && events.isNotEmpty())
            throw DownloaderEventsError(events)

        return@withContext enqueuedCount
    }
}
