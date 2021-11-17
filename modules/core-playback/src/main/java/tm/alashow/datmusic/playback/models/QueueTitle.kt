/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.content.res.Resources
import kotlinx.serialization.Serializable
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.datmusic.playback.R
import tm.alashow.domain.models.JSON

@Serializable
data class QueueTitle(
    val sourceMediaId: MediaId = MediaId(),
    val type: Type = Type.UNKNOWN,
    val value: String? = null
) {

    fun localizeType(resources: Resources): String = when (type) {
        Type.UNKNOWN, Type.AUDIO -> resources.getString(R.string.playback_queueTitle_audio)
        Type.ARTIST -> resources.getString(R.string.playback_queueTitle_artist)
        Type.ALBUM -> resources.getString(R.string.playback_queueTitle_album)
        Type.PLAYLIST -> resources.getString(R.string.playback_queueTitle_playlist)
        Type.SEARCH -> resources.getString(R.string.playback_queueTitle_search)
        Type.DOWNLOADS -> resources.getString(R.string.playback_queueTitle_downloads)
    }

    fun localizeValue(): String = when (type) {
        Type.UNKNOWN, Type.AUDIO, Type.DOWNLOADS -> ""
        Type.ARTIST, Type.ALBUM, Type.PLAYLIST, Type.SEARCH -> value ?: ""
    }

    override fun toString() = JSON.encodeToString(serializer(), this)

    companion object {
        fun String?.asQueueTitle() = orBlank().let { value ->
            try {
                JSON.decodeFromString(serializer(), value)
            } catch (e: Exception) {
                QueueTitle()
            }
        }
    }

    enum class Type {
        UNKNOWN, AUDIO, ALBUM, ARTIST, SEARCH, DOWNLOADS, PLAYLIST;

        companion object {
            private val map = values().associateBy { it.name }

            fun from(value: String?) = map[value] ?: UNKNOWN
        }
    }
}
