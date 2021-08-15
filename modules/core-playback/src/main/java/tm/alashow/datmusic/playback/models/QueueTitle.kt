/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.content.Context
import tm.alashow.datmusic.playback.R

data class QueueTitle(val type: Type = Type.UNKNOWN, val value: String? = null) {

    override fun toString() = type.name + separator + (value ?: "")

    fun localizeType(context: Context): String = when (type) {
        Type.UNKNOWN, Type.AUDIO -> context.getString(R.string.playback_queueTitle_audio)
        Type.ARTIST -> context.getString(R.string.playback_queueTitle_artist)
        Type.ALBUM -> context.getString(R.string.playback_queueTitle_album)
        Type.SEARCH -> context.getString(R.string.playback_queueTitle_search)
        Type.DOWNLOADS -> context.getString(R.string.playback_queueTitle_downloads)
    }

    fun localizeValue(context: Context): String = when (type) {
        Type.UNKNOWN, Type.AUDIO, Type.DOWNLOADS -> ""
        Type.ARTIST, Type.ALBUM -> value ?: ""
        Type.SEARCH -> if (value != null) """"$value"""" else ""
    }

    companion object {
        private const val separator = "$$"

        fun from(title: String) = title.split(separator).let { parts ->
            try {
                QueueTitle(
                    Type.from(parts[0]),
                    parts[1].let {
                        if (it.isBlank()) null
                        else it
                    }
                )
            } catch (e: Exception) {
                QueueTitle()
            }
        }
    }

    enum class Type {
        UNKNOWN, AUDIO, ALBUM, ARTIST, SEARCH, DOWNLOADS;

        companion object {
            private val map = values().associateBy { it.name }

            fun from(value: String?) = map[value] ?: UNKNOWN
        }
    }
}
