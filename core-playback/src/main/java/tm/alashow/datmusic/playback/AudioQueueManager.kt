/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.support.v4.media.session.MediaSessionCompat
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.swap
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Audio

interface AudioQueueManager {
    var currentAudioId: String
    var currentAudio: Audio?

    var queue: List<String>
    var queueTitle: String

    val previousAudioId: String?
    val nextAudioId: String?

    suspend fun currentAudio(): Audio?

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
    private val preferences: PreferencesStore,
) : AudioQueueManager, CoroutineScope by MainScope() {

    companion object {
        val originalQueueKey = stringSetPreferencesKey("player_audio_queue_original")
    }

    private lateinit var mediaSession: MediaSessionCompat
    private val playedAudios = mutableListOf<String>()
    private val auxQueue = mutableListOf<String>()

    private val currentAudioIndex
        get() = queue.indexOf(currentAudioId)

    override var currentAudioId: String = ""
    override var currentAudio: Audio? = null

    override suspend fun currentAudio(): Audio? {
        currentAudio = audiosDao.entry(currentAudioId).firstOrNull()
        return currentAudio
    }

    override var queue: List<String> = emptyList()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                launch {
                    mediaSession.setQueue(audiosDao.entriesById(value).firstOrNull()?.toQueue())
                    auxQueue.clear()
                    auxQueue.addAll(preferences.get(originalQueueKey, setOf()).first().toList())
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
        if (isShuffle) mediaSession.setQueue(shuffleQueue())
        else restoreQueueOrder()
    }

    private fun shuffleQueue(): List<MediaSessionCompat.QueueItem> {
        val sQueue = mediaSession.controller.queue.shuffled()
        val realQueue = sQueue.swap(sQueue.indexOfFirst { it.description.mediaId == currentAudioId }, 0)

        auxQueue.clear()
        auxQueue.addAll(queue.toList())
        launch { preferences.save(originalQueueKey, auxQueue.toSet()) }
        queue = realQueue.toMediaIdList()

        return sQueue
    }

    private fun restoreQueueOrder() {
        queue = auxQueue
        auxQueue.clear()
    }
}
