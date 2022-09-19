/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.ui.R

sealed class AudioDownloadItemAction(open val audio: AudioDownloadItem) {
    data class Play(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class PlayNext(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Resume(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Pause(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Cancel(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Retry(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Open(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Remove(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Delete(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class CopyLink(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class AddToPlaylist(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)

    companion object {
        fun from(actionLabelRes: Int, audio: AudioDownloadItem) = when (actionLabelRes) {
            R.string.downloads_download_play -> Play(audio)
            R.string.downloads_download_playNext -> PlayNext(audio)
            R.string.downloads_download_pause -> Pause(audio)
            R.string.downloads_download_resume -> Resume(audio)
            R.string.downloads_download_cancel -> Cancel(audio)
            R.string.downloads_download_retry -> Retry(audio)
            R.string.downloads_download_open -> Open(audio)
            R.string.downloads_download_remove -> Remove(audio)
            R.string.downloads_download_delete -> Delete(audio)
            R.string.audio_menu_copyLink -> CopyLink(audio)
            R.string.playlist_addTo -> AddToPlaylist(audio)
            else -> error("Unknown action: $actionLabelRes")
        }
    }
}
