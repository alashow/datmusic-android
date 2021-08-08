/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.BuildConfig
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler
import tm.alashow.datmusic.ui.downloader.DownloaderHost
import tm.alashow.datmusic.ui.downloads.LocalAudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloads.audioDownloadItemActionHandler
import tm.alashow.datmusic.ui.home.Home
import tm.alashow.datmusic.ui.playback.PlaybackHost
import tm.alashow.datmusic.ui.settings.LocalAppVersion
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.DefaultTheme

@Composable
fun DatmusicApp(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(LocalContext.current),
) {

    CompositionLocalProvider(
        LocalScaffoldState provides scaffoldState,
        LocalAnalytics provides analytics,
        LocalAppVersion provides BuildConfig.VERSION_NAME
    ) {
        val themeViewModel = hiltViewModel<ThemeViewModel>()
        val themeState by rememberFlowWithLifecycle(themeViewModel.themeState).collectAsState(DefaultTheme)

        AppTheme(themeState) {
            ProvideWindowInsets(consumeWindowInsets = false) {
                DatmusicCore {
                    Home()
                }
            }
        }
    }
}

@Composable
private fun DatmusicCore(content: @Composable () -> Unit) {
    DownloaderHost {
        PlaybackHost {
            DatmusicActionHandlers {
                content()
            }
        }
    }
}

@Composable
private fun DatmusicActionHandlers(content: @Composable () -> Unit) {
    val audioActionHandler = audioActionHandler()
    val audioDownloadItemActionHandler = audioDownloadItemActionHandler()
    CompositionLocalProvider(
        LocalAudioActionHandler provides audioActionHandler,
        LocalAudioDownloadItemActionHandler provides audioDownloadItemActionHandler
    ) {
        content()
    }
}
