/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.coreDomain.R
import tm.alashow.domain.models.JSON
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.ValidationError

const val BACKUP_CURRENT_VERSION = 1

data class DatmusicBackupVersionValidation(val version: Int) : ValidationError(
    UiMessage.Resource(
        R.string.settings_database_restore_NonMatchingVersion,
        listOf(BACKUP_CURRENT_VERSION, version)
    )
) {
    override fun isValid() = version == BACKUP_CURRENT_VERSION
}

@Serializable
data class DatmusicBackupData(
    val audios: Audios,
    val playlists: Playlists,
    val playlistAudios: PlaylistAudios,

    @SerialName("backup_version")
    private val version: Int,
) {

    fun checkVersion() {
        DatmusicBackupVersionValidation(version).validate()
    }

    fun toJson() = JSON.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String) = JSON.decodeFromString(serializer(), json)

        fun create(
            audios: Audios,
            playlists: Playlists,
            playlistAudios: PlaylistAudios
        ) = DatmusicBackupData(audios, playlists, playlistAudios, BACKUP_CURRENT_VERSION)
    }
}
