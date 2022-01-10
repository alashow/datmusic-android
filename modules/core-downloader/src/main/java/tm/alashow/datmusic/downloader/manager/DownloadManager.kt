/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.manager

sealed class DownloadEnqueueResult<T>
data class DownloadEnqueueSuccessful<T>(val updatedRequest: T) : DownloadEnqueueResult<T>()
data class DownloadEnqueueFailed<T>(val error: Throwable) : DownloadEnqueueResult<T>()

interface DownloadManager<ID, RequestType, StatusType, DownloadType> {
    suspend fun enqueue(request: RequestType): DownloadEnqueueResult<RequestType>

    suspend fun getDownload(id: ID): DownloadType?

    suspend fun getDownloads(): List<DownloadType>
    suspend fun getDownloadsWithIdsAndStatuses(ids: Set<ID>, statuses: List<StatusType>): List<DownloadType>
    suspend fun getDownloadsWithStatuses(statuses: List<StatusType>): List<DownloadType>

    suspend fun pause(id: ID)
    suspend fun resume(id: ID)
    suspend fun cancel(id: ID)
    suspend fun retry(id: ID)
    suspend fun remove(id: ID)
    suspend fun delete(id: ID)

    suspend fun pause(ids: List<ID>)
    suspend fun resume(ids: List<ID>)
    suspend fun cancel(ids: List<ID>)
    suspend fun retry(ids: List<ID>)
    suspend fun remove(ids: List<ID>)
    suspend fun delete(ids: List<ID>)
}
