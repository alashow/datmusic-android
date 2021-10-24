/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.domain.entities.PlaylistAudios
import tm.alashow.datmusic.domain.entities.Playlists

const val BACKUP_CURRENT_VERSION = 1

@Serializable
data class DatmusicBackupData(
    val audios: Audios,
    val playlists: Playlists,
    val playlistAudios: PlaylistAudios,
    private val version: Int = BACKUP_CURRENT_VERSION
) {
    init {
        if (version != BACKUP_CURRENT_VERSION)
            Timber.e("Backup data has non-matching version number: current=$BACKUP_CURRENT_VERSION, backup_version=$version")
    }

    fun toJson() = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString(serializer(), json)
    }
}
