/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.media.R

sealed class AudioItemAction(open val audio: Audio) {
    data class Play(override val audio: Audio) : AudioItemAction(audio)
    data class Download(override val audio: Audio) : AudioItemAction(audio)
    data class DownloadById(override val audio: Audio) : AudioItemAction(audio)
    data class CopyLink(override val audio: Audio) : AudioItemAction(audio)

    companion object {
        fun from(actionLabelRes: Int, audio: Audio) = when (actionLabelRes) {
            R.string.audio_menu_play -> Play(audio)
            R.string.audio_menu_download -> Download(audio)
            R.string.audio_menu_downloadById -> DownloadById(audio)
            R.string.audio_menu_copyLink -> CopyLink(audio)
            else -> error("Unknown action: $actionLabelRes")
        }
    }
}
