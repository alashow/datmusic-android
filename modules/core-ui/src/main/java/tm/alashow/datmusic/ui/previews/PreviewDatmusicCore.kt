/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalAppVersion
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.LocalSnackbarHostState
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.downloader.LocalAudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloader.LocalDownloader
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.datmusic.ui.snackbar.SnackbarMessagesHost
import tm.alashow.navigation.LocalNavigator
import tm.alashow.ui.theme.DefaultTheme
import tm.alashow.ui.theme.PreviewAppTheme

@Composable
fun PreviewDatmusicCore(
    theme: ThemeState = DefaultTheme,
    changeSystemBar: Boolean = true,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {
    AndroidThreeTen.init(context)
    LaunchedEffect(Unit) {
        Timber.plant(Timber.DebugTree())
    }

    CompositionLocalProvider(
        LocalNavigator provides PreviewNavigator,
        LocalDownloader provides PreviewDownloader,
        LocalPlaybackConnection provides previewPlaybackConnection(),
        LocalAudioActionHandler provides PreviewAudioActionHandler,
        LocalAudioDownloadItemActionHandler provides PreviewAudioDownloadItemActionHandler,
        LocalAnalytics provides PreviewAnalytics,
        LocalSnackbarHostState provides snackbarHostState,
        LocalAppVersion provides "1.0.0",
        LocalIsPreviewMode provides true,
    ) {
        SnackbarMessagesHost(viewModel = PreviewSnackbarMessagesHostViewModel)
        PreviewAppTheme(
            content = content,
            theme = theme,
            changeSystemBar = changeSystemBar,
        )
    }
}
