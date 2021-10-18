/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.audio

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.AudiosDao
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
) : RoomRepo<AudioId, Audio>(dao, dispatchers) {

    fun audiosById(ids: AudioIds) = dao.audiosById(ids).flowOn(dispatchers.io)

    suspend fun saveAudiosById(type: AudioSaveType, audioIds: AudioIds) = saveAudios(type, audiosById(audioIds).first())

    suspend fun saveAudios(type: AudioSaveType, audios: Audios) = saveAudios(type, *audios.toTypedArray())

    suspend fun saveAudios(type: AudioSaveType, vararg audios: Audio): Int {
        val mapped = audios.map { it.copy(primaryKey = it.id, params = type.toAudioParams()) }
        return insert(mapped).size
    }
}
