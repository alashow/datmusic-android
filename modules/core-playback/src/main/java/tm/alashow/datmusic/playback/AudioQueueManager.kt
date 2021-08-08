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
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.extensions.swap
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.db.daos.findAudios
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
    fun skipTo(position: Int)
    fun remove(position: Int)
    fun remove(id: String)
    fun swap(from: Int, to: Int)
    fun queue(): String
    fun clear()
    fun clearPlayedAudios()
    fun shuffleQueue(isShuffle: Boolean = false)
}

class AudioQueueManagerImpl @Inject constructor(
    private val audiosDao: AudiosDao,
    private val downloadsDao: DownloadRequestsDao,
    private val downloader: Downloader,
    private val dispatchers: CoroutineDispatchers,
) : AudioQueueManager, CoroutineScope by MainScope() {

    private lateinit var mediaSession: MediaSessionCompat
    private val playedAudios = mutableListOf<String>()
    private var originalQueue = listOf<String>()

    override var currentAudioId: String = ""
    override var currentAudio: Audio? = null

    private val currentAudioIndex get() = queue.indexOf(currentAudioId)

    override suspend fun refreshCurrentAudio(): Audio? {
        currentAudio = audiosDao.entry(currentAudioId).firstOrNull()?.apply {
            audioDownloadItem = downloader.getAudioDownload(id, Status.COMPLETED).orNull()
        }
        return currentAudio
    }

    override var queue: List<String> = listOf()
        set(value) {
            val deduped = value.toSet().toList()
            field = deduped
            setQueueItems(deduped)
        }

    override var queueTitle: String = ""
        set(value) {
            field = value
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

    private fun setQueueItems(ids: List<String>) {
        if (ids.isNotEmpty()) {
            launch {
                withContext(dispatchers.computation) {
                    val orderByIndex = ids.withIndex().associate { it.value to it.index }
                    val audios = (audiosDao to downloadsDao).findAudios(ids).sortedBy { orderByIndex[it.id] }
                    mediaSession.setQueue(audios.toQueueItems())
                }
            }
        }
    }

    override fun setMediaSession(session: MediaSessionCompat) {
        mediaSession = session
    }

    override fun playNext(id: String) {
        val nextIndex = currentAudioIndex + 1
        queue = queue.toMutableList().apply {
            add(nextIndex, id)
        }
    }

    override fun skipTo(position: Int) {
        currentAudioId = queue[position]
    }

    override fun remove(position: Int) {
        queue = queue.toMutableList().apply { removeAt(position) }
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
            withContext(dispatchers.computation) {
                if (isShuffle) shuffleQueue()
                else restoreQueueOrder()
            }
        }
    }

    private fun shuffleQueue() {
        val shuffled = queue.let { original ->
            original.shuffled().let { shuffled ->
                val currentIdIndex = shuffled.indexOfFirst { id -> id == currentAudioId }
                if (currentIdIndex >= 0)
                    shuffled.swap(currentIdIndex, 0)
                else {
                    Timber.e("CurrentIdIndex is not found found")
                    return
                }
            }
        }

        Timber.d("Saving shuffled queue: ${shuffled.size}")

        // save non-shuffled original queue
        originalQueue = queue
        // set and return shuffled queue
        queue = shuffled
    }

    private fun restoreQueueOrder() {
        queue = originalQueue
    }
}
