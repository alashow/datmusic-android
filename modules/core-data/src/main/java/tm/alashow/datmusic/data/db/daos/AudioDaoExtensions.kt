/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db.daos

import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import tm.alashow.datmusic.domain.entities.Audio

suspend fun Pair<AudiosDao, DownloadRequestsDao>.findAudio(id: String): Audio? = findAudios(listOf(id)).firstOrNull()

@OptIn(ExperimentalStdlibApi::class)
suspend fun Pair<AudiosDao, DownloadRequestsDao>.findAudios(ids: List<String>): List<Audio> {
    val audios = first.entriesById(ids.toList()).firstOrNull().orEmpty().map { it.id to it }.toMap()
    val downloads = second.entriesById(ids.toList()).firstOrNull().orEmpty().map { it.audio.id to it.audio }.toMap()
    return buildList {
        ids.forEach { id ->
            val audio = audios[id] ?: downloads[id]
            if (audio == null) {
                Timber.e("Couldn't find audio by id: $id")
            } else add(audio)
        }
    }
}
