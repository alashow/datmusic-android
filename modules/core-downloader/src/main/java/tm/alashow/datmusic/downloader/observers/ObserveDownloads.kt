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
import tm.alashow.datmusic.downloader.R
import tm.alashow.datmusic.downloader.downloads
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.asAsyncFlow
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.ValidationErrorException

data class NoResultsForDownloadsFilter(val params: ObserveDownloads.Params) :
    ValidationErrorException(
        ValidationError(
            when (params.hasNoQuery) {
                true -> UiMessage.Resource(R.string.downloads_filter_noResults)
                false -> UiMessage.Resource(R.string.downloads_filter_noResults_forQuery, listOf(params.query))
            }
        )
    )

class ObserveDownloads @Inject constructor(
    private val fetcher: Fetch,
    private val dao: DownloadRequestsDao,
    private val audiosFtsDao: AudiosFtsDao,
) : SubjectInteractor<ObserveDownloads.Params, Async<DownloadItems>>() {

    data class Params(val query: String = "") {
        val hasNoQuery get() = query.isBlank()
        val isEmpty get() = hasNoQuery
    }

    private fun fetcherDownloads() = flow {
        while (true) {
            emit(fetcher.downloads())
            delay(Downloader.DOWNLOADS_STATUS_REFRESH_INTERVAL)
        }
    }.distinctUntilChanged()

    override fun createObservable(params: Params): Flow<Async<DownloadItems>> {
        val downloadsRequestsFlow = when {
            params.isEmpty -> dao.entries()
            else -> audiosFtsDao.searchDownloads("*${params.query}*")
        }

        return combine(downloadsRequestsFlow, fetcherDownloads()) { downloadRequests, downloads ->
            if (downloadRequests.isEmpty() && !params.isEmpty) {
                throw NoResultsForDownloadsFilter(params)
            }

            val audioRequests = downloadRequests.filter { it.entityType == DownloadRequest.Type.Audio }
            val audioDownloads = audioRequests.map { request ->
                val downloadInfo = downloads.firstOrNull { dl -> dl.id == request.requestId }
                AudioDownloadItem.from(request, request.audio, downloadInfo)
            }

            DownloadItems(audioDownloads)
        }.asAsyncFlow()
    }
}
