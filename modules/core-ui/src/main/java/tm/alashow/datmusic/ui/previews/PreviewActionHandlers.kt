/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import timber.log.Timber
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.AudioItemAction

internal val PreviewAudioActionHandler: AudioActionHandler = { action: AudioItemAction ->
    Timber.d("PreviewAudioActionHandler: $action")
}
