/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import androidx.compose.runtime.staticCompositionLocalOf
import tm.alashow.datmusic.downloader.Downloader

val LocalDownloader = staticCompositionLocalOf<Downloader> {
    error("LocalDownloader not provided")
}
