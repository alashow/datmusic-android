/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAudioActionHandler = staticCompositionLocalOf<AudioActionHandler> {
    error("No LocalAudioActionHandler provided")
}

typealias AudioActionHandler = (AudioItemAction) -> Unit
