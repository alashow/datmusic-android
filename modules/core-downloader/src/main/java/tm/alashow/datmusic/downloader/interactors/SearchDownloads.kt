/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.interactors

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.db.daos.AudiosFtsDao
import tm.alashow.datmusic.domain.entities.DownloadRequest

class SearchDownloads @Inject constructor(
    private val audiosFtsDao: AudiosFtsDao,
) : SubjectInteractor<String, List<DownloadRequest>>() {
    override fun createObservable(params: String): Flow<List<DownloadRequest>> {
        return audiosFtsDao.searchDownloads("*$params*")
    }
}
