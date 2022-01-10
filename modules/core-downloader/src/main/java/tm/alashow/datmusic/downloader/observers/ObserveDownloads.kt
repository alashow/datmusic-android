/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.interactors.SearchDownloads
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Success

class ObserveDownloads @Inject constructor(
    private val fetcher: FetchDownloadManager,
    private val dao: DownloadRequestsDao,
    private val searchDownloads: SearchDownloads,
) : SubjectInteractor<ObserveDownloads.Params, DownloadItems>() {

    data class Params(
        val query: String = "",
        val audiosSortOptions: List<DownloadAudioItemSortOption> = DownloadAudioItemSortOptions.ALL,
        val defaultSortOption: DownloadAudioItemSortOption = DownloadAudioItemSortOptions.ALL.first(),
        val audiosSortOption: DownloadAudioItemSortOption = defaultSortOption,
        val defaultStatusFilters: HashSet<DownloadStatusFilter> = hashSetOf(DownloadStatusFilter.All),
        val statusFilters: HashSet<DownloadStatusFilter> = defaultStatusFilters,
    ) {
        val hasQuery get() = query.isNotBlank()
        val hasSortingOption get() = audiosSortOption != defaultSortOption
        val hasStatusFilter get() = statusFilters != defaultStatusFilters
        val hasNoFilters get() = !hasQuery && !hasSortingOption && !hasStatusFilter

        val statuses get() = statusFilters.map { it.statuses }.flatten()
    }

    private fun fetcherDownloads(
        downloadRequests: List<DownloadRequest> = emptyList(),
        statuses: List<Status>
    ): Flow<List<Pair<DownloadRequest, Download>>> = flow {
        val requestsById = downloadRequests.associateBy { it.requestId }
        while (true) {
            fetcher.getDownloadsWithIdsAndStatuses(ids = requestsById.keys, statuses = statuses)
                .map { requestsById.getValue(it.id) to it }
                .also { emit(it) }
            delay(Downloader.DOWNLOADS_STATUS_REFRESH_INTERVAL)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun createObservable(params: Params): Flow<DownloadItems> {
        val downloadsRequestsFlow = when {
            params.hasQuery -> searchDownloads("*${params.query}*")
            else -> dao.entries()
        }

        return downloadsRequestsFlow.flatMapLatest { fetcherDownloads(it, params.statuses) }
            .map {
                val audioDownloads = it.filter { pair -> pair.first.entityType == DownloadRequest.Type.Audio }
                    .map { (request, info) -> AudioDownloadItem.from(request, request.audio, info) }
                    .sortedWith(params.audiosSortOption.comparator)
                DownloadItems(audioDownloads)
            }
    }
}

fun Async<DownloadItems>.failWithNoResultsIfEmpty(params: ObserveDownloads.Params) = let {
    if (it is Success) {
        if (it().audios.isEmpty() && !params.hasNoFilters)
            Fail(NoResultsForDownloadsFilter(params))
        else it
    } else it
}
