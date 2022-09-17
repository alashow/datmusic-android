/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import javax.inject.Inject
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.DownloadRequest

class DownloadRequestsRepo @Inject constructor(
    dispatchers: CoroutineDispatchers,
    dao: DownloadRequestsDao,
) : RoomRepo<String, DownloadRequest>(dao, dispatchers)
