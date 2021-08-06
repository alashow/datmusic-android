/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.support.v4.media.session.MediaSessionCompat
import com.tonyodev.fetch2.Status
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.swap
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.models.toQueueItems
import tm.alashow.domain.models.orNull

interface AudioQueueManager {
    var currentAudioId: String
    var currentAudio: Audio?

    var queue: List<String>
    var queueTitle: String

    val previousAudioId: String?
    val nextAudioId: String?

    suspend fun refreshCurrentAudio(): Audio?

    fun setMediaSession(session: MediaSessionCompat)
    fun playNext(id: String)
    fun remove(id: String)
    fun swap(from: Int, to: Int)
    fun queue(): String
    fun clear()
    fun clearPlayedAudios()
    fun shuffleQueue(isShuffle: Boolean = false)
}

class AudioQueueManagerImpl @Inject constructor(
    private val audiosDao: AudiosDao,
    private val downloadRequestsDao: DownloadRequestsDao,
    private val downloader: Downloader,
) : AudioQueueManager, CoroutineScope by MainScope() {

    private lateinit var mediaSession: MediaSessionCompat
    private val playedAudios = mutableListOf<String>()
    private var originalQueue = listOf<String>()

    private val currentAudioIndex
        get() = queue.indexOf(currentAudioId)

    override var currentAudioId: String = ""
    override var currentAudio: Audio? = null

    override suspend fun refreshCurrentAudio(): Audio? {
        currentAudio = audiosDao.entry(currentAudioId).firstOrNull()?.apply {
            audioDownloadItem = downloader.getAudioDownload(id, Status.COMPLETED).orNull()
        }
        return currentAudio
    }

    override var queue: List<String> = listOf()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                launch {
                    mediaSession.setQueue(audiosDao.entriesById(value).firstOrNull()?.toQueueItems())
                }
            }
        }

    override var queueTitle: String = ""
        set(value) {
            field = if (value.isNotEmpty()) {
                value
            } else "All"

            mediaSession.setQueueTitle(value)
        }

    override val previousAudioId: String?
        get() {
            if (mediaSession.position() >= 5000) return currentAudioId
            val previousIndex = currentAudioIndex - 1

            return when {
                previousIndex >= 0 -> queue[previousIndex]
                else -> null
            }
        }

    override val nextAudioId: String?
        get() {
            val nextIndex = currentAudioIndex + 1
            return when {
                nextIndex < queue.size -> queue[nextIndex]
                else -> null
            }
        }

    override fun setMediaSession(session: MediaSessionCompat) {
        mediaSession = session
    }

    override fun playNext(id: String) {
        val nextIndex = currentAudioIndex + 1
        swap(queue.indexOf(id), nextIndex)
    }

    override fun remove(id: String) {
        queue = queue.toMutableList().apply { remove(id) }
    }

    override fun swap(from: Int, to: Int) {
        queue = queue.swap(from, to)
    }

    override fun queue(): String {
        return "${currentAudioIndex + 1}/${queue.size}"
    }

    override fun clear() {
        queue = emptyList()
        queueTitle = ""
        currentAudioId = ""
    }

    override fun clearPlayedAudios() {
        playedAudios.clear()
    }

    override fun shuffleQueue(isShuffle: Boolean) {
        launch {
            if (isShuffle) mediaSession.setQueue(shuffleQueue())
            else restoreQueueOrder()
        }
    }

    private suspend fun shuffleQueue(): List<MediaSessionCompat.QueueItem> {
        val shuffled = queue.let { original ->
            original.shuffled().let { shuffled ->
                val currentIdIndex = shuffled.indexOfFirst { id -> id == currentAudioId }
                if (currentIdIndex >= 0)
                    shuffled.swap(currentIdIndex, 0)
                else {
                    Timber.e("CurrentIdIndex is not found found")
                    return emptyList()
                }
            }
        }

        Timber.d("Saving shuffled queue: ${shuffled.size}")

        // save non-shuffled original queue
        originalQueue = queue
        // set and return shuffled queue
        queue = shuffled
        return audiosDao.entriesById(queue).firstOrNull()?.toQueueItems() ?: error("Null entries")
    }

    private fun restoreQueueOrder() {
        queue = originalQueue
    }
}
