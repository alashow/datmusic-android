/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import timber.log.Timber
import tm.alashow.datmusic.ui.downloads.AudioDownloadItemAction

internal val PreviewAudioDownloadItemActionHandler = { action: AudioDownloadItemAction ->
    Timber.d("PreviewAudioDownloadItemActionHandler: $action")
}
