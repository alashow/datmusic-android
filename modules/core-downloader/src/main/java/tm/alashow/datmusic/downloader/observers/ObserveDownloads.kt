/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status
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

class ObserveDownloads @Inject constructor(
    private val fetcher: Fetch,
    private val dao: DownloadRequestsDao,
    private val audiosFtsDao: AudiosFtsDao,
) : SubjectInteractor<ObserveDownloads.Params, DownloadItems>() {

    data class Params(
        val query: String = "",
        val audiosSortOptions: List<DownloadAudioItemSortOption> = DownloadAudioItemSortOptions.ALL,
        val defaultSortOption: DownloadAudioItemSortOption = DownloadAudioItemSortOptions.ALL.first(),
        val audiosSortOption: DownloadAudioItemSortOption = defaultSortOption,
        val defaultStatusFilters: Set<DownloadStatusFilter> = setOf(DownloadStatusFilter.All),
        val statusFilters: Set<DownloadStatusFilter> = defaultStatusFilters,
    ) {
        val hasQuery get() = query.isNotBlank()
        val hasSortingOption get() = audiosSortOption != defaultSortOption
        val hasStatusFilter get() = statusFilters != defaultStatusFilters
        val hasNoFilters get() = !hasQuery && !hasSortingOption && !hasStatusFilter

        val statuses get() = statusFilters.map { it.statuses }.flatten()
    }

    private fun fetcherDownloads(statusFilters: List<Status>) = flow {
        while (true) {
            emit(fetcher.downloads(statusFilters))
            delay(Downloader.DOWNLOADS_STATUS_REFRESH_INTERVAL)
        }
    }.distinctUntilChanged()

    override fun createObservable(params: Params): Flow<DownloadItems> {
        val downloadsRequestsFlow = when {
            params.hasQuery -> audiosFtsDao.searchDownloads("*${params.query}*")
            else -> dao.entries()
        }

        return combine(downloadsRequestsFlow, fetcherDownloads(params.statuses)) { downloadRequests, downloads ->
            val audioRequests = downloadRequests.filter { it.entityType == DownloadRequest.Type.Audio }
            val audioDownloads = audioRequests
                .map { request ->
                    val downloadInfo = downloads.firstOrNull { dl -> dl.id == request.requestId }
                    AudioDownloadItem.from(request, request.audio, downloadInfo)
                }
                .filter {
                    if (!params.hasStatusFilter) true
                    else params.statuses.contains(it.downloadInfo.status)
                }
                .let {
                    if (it.isEmpty() && !params.hasNoFilters)
                        throw NoResultsForDownloadsFilter(params)
                    val comparator = params.audiosSortOption.comparator
                    if (comparator != null) it.sortedWith(comparator)
                    else it
                }

            DownloadItems(audioDownloads)
        }
    }
}
