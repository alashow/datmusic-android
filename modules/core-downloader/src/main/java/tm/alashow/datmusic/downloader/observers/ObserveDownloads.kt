/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import com.tonyodev.fetch2.Fetch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.db.daos.AudiosFtsDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.downloads
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.asAsyncFlow

class ObserveDownloads @Inject constructor(
    private val fetcher: Fetch,
    private val dao: DownloadRequestsDao,
    private val audiosFtsDao: AudiosFtsDao,
) : SubjectInteractor<ObserveDownloads.Params, Async<DownloadItems>>() {

    data class Params(
        val query: String = "",
        val audiosSortOptions: List<DownloadAudioItemSortOption> = DownloadAudioItemSortOptions.ALL,
        val defaultSortOption: DownloadAudioItemSortOption = DownloadAudioItemSortOptions.ALL.first(),
        val audiosSortOption: DownloadAudioItemSortOption = defaultSortOption,
    ) {
        val hasQuery get() = query.isNotBlank()
        val hasSortingOption get() = audiosSortOption != defaultSortOption
        val isEmpty get() = !hasQuery && !hasSortingOption
    }

    private fun fetcherDownloads() = flow {
        while (true) {
            emit(fetcher.downloads())
            delay(Downloader.DOWNLOADS_STATUS_REFRESH_INTERVAL)
        }
    }.distinctUntilChanged()

    override fun createObservable(params: Params): Flow<Async<DownloadItems>> {
        val downloadsRequestsFlow = when {
            params.hasQuery -> audiosFtsDao.searchDownloads("*${params.query}*")
            else -> dao.entries()
        }

        return combine(downloadsRequestsFlow, fetcherDownloads()) { downloadRequests, downloads ->
            if (downloadRequests.isEmpty() && !params.isEmpty) {
                throw NoResultsForDownloadsFilter(params)
            }

            val audioRequests = downloadRequests.filter { it.entityType == DownloadRequest.Type.Audio }
            val audioDownloads = audioRequests
                .map { request ->
                    val downloadInfo = downloads.firstOrNull { dl -> dl.id == request.requestId }
                    AudioDownloadItem.from(request, request.audio, downloadInfo)
                }
                .let {
                    val comparator = params.audiosSortOption.comparator
                    if (comparator != null) it.sortedWith(comparator)
                    else it
                }

            DownloadItems(audioDownloads)
        }.distinctUntilChanged().asAsyncFlow()
    }
}
