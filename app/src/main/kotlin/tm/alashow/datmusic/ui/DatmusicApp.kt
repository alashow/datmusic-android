/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.base.util.Analytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalAppVersion
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.LocalSnackbarHostState
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.previews.FontScalePreview
import tm.alashow.common.compose.previews.LocalePreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.BuildConfig
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler
import tm.alashow.datmusic.ui.downloader.AudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloader.DownloaderHost
import tm.alashow.datmusic.ui.downloader.LocalAudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloads.audioDownloadItemActionHandler
import tm.alashow.datmusic.ui.home.Home
import tm.alashow.datmusic.ui.playback.PlaybackHost
import tm.alashow.datmusic.ui.playback.PlaybackViewModel
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.datmusic.ui.snackbar.SnackbarMessagesHost
import tm.alashow.navigation.NavigatorHost
import tm.alashow.navigation.activityHiltViewModel
import tm.alashow.navigation.rememberBottomSheetNavigator
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.theme.AppTheme

@Composable
fun DatmusicApp(
    playbackViewModel: PlaybackViewModel = activityHiltViewModel(),
) = DatmusicCore {
    DatmusicAppContent(
        onPlayingTitleClick = playbackViewModel::onTitleClick,
        onPlayingArtistClick = playbackViewModel::onArtistClick,
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun DatmusicAppContent(
    onPlayingTitleClick: () -> Unit,
    onPlayingArtistClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    ModalBottomSheetLayout(bottomSheetNavigator, modifier) {
        Home(
            navController = navController,
            onPlayingTitleClick = onPlayingTitleClick,
            onPlayingArtistClick = onPlayingArtistClick,
        )
    }
}

// Could be renamed to DatmusicCoreViewModel if more things are injected
@HiltViewModel
private class AnalyticsViewModel @Inject constructor(val analytics: Analytics) : ViewModel()

@Composable
private fun DatmusicCore(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    themeViewModel: ThemeViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    appVersion: String = BuildConfig.VERSION_NAME,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalAnalytics provides analyticsViewModel.analytics,
        LocalAppVersion provides appVersion,
        LocalIsPreviewMode provides false,
    ) {
        SnackbarMessagesHost()
        val themeState by rememberFlowWithLifecycle(themeViewModel.themeState)
        AppTheme(themeState, modifier) {
            NavigatorHost {
                DownloaderHost {
                    PlaybackHost {
                        DatmusicActionHandlers(content = content)
                    }
                }
            }
        }
    }
}

@Composable
private fun DatmusicActionHandlers(
    content: @Composable () -> Unit,
    audioActionHandler: AudioActionHandler = audioActionHandler(),
    audioDownloadItemActionHandler: AudioDownloadItemActionHandler = audioDownloadItemActionHandler(),
) {
    CompositionLocalProvider(
        LocalAudioActionHandler provides audioActionHandler,
        LocalAudioDownloadItemActionHandler provides audioDownloadItemActionHandler,
    ) {
        content()
    }
}

@CombinedPreview
@LocalePreview
@FontScalePreview
@Composable
fun DatmusicAppPreview() = PreviewDatmusicCore {
    DatmusicAppContent(
        onPlayingTitleClick = {},
        onPlayingArtistClick = {}
    )
}
