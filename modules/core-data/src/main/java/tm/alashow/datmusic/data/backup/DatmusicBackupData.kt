/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.domain.entities.PlaylistAudios
import tm.alashow.datmusic.domain.entities.Playlists

@Serializable
data class DatmusicBackupData(
    val audios: Audios,
    val playlists: Playlists,
    val playlistAudios: PlaylistAudios
) {
    fun toJson() = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString(serializer(), json)
    }
}
