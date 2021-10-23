/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.audio

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioId
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.Audios

enum class AudioSaveType {
    Download, Playlist;

    fun toAudioParams() = "save_type=${toString()}"
}

class AudiosRepo @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val dao: AudiosDao,
    private val downloadsRequestsDao: DownloadRequestsDao,
) : RoomRepo<AudioId, Audio>(dao, dispatchers) {

    fun audiosById(ids: AudioIds) = dao.audiosById(ids).flowOn(dispatchers.io)

    suspend fun saveAudiosById(type: AudioSaveType, audioIds: AudioIds) = saveAudios(type, audiosById(audioIds).first())

    suspend fun saveAudios(type: AudioSaveType, audios: Audios) = saveAudios(type, *audios.toTypedArray())

    suspend fun saveAudios(type: AudioSaveType, vararg audios: Audio): Int {
        val mapped = audios.map { it.copy(primaryKey = it.id, params = type.toAudioParams()) }
        return insert(mapped).size
    }

    suspend fun find(audioId: String): Audio? = find(listOf(audioId)).firstOrNull()

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun find(ids: AudioIds): List<Audio> {
        val audios = audiosById(ids).firstOrNull().orEmpty().map { it.id to it }.toMap()
        val downloads = downloadsRequestsDao.entriesById(ids).firstOrNull().orEmpty().map { it.audio.id to it.audio }.toMap()
        return buildList {
            ids.forEach { id ->
                val audio = audios[id] ?: downloads[id]
                if (audio == null) {
                    Timber.e("Couldn't find audio by id: $id")
                } else add(audio)
            }
        }
    }
}
