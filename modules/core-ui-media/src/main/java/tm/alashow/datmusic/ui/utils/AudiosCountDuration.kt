/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.utils

import android.content.res.Resources
import tm.alashow.base.util.extensions.interpunctize
import tm.alashow.base.util.localizeDuration
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.ui.media.R
import tm.alashow.i18n.TextCreator

data class AudiosCountDuration(val count: Int, val duration: Long = Long.MAX_VALUE) {
    companion object {
        fun from(audios: Audios) = AudiosCountDuration(audios.size, audios.sumOf { it.durationMillis() })
    }
}

object AudiosCountDurationTextCreator : TextCreator<AudiosCountDuration> {

    override fun AudiosCountDuration.localize(resources: Resources): String {
        val count = resources.getQuantityString(R.plurals.songs_count, count, count)
        val duration = resources.localizeDuration(duration, true)
        return listOf(count, duration).interpunctize()
    }
}
