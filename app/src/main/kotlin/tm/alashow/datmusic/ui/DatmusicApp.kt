/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.BuildConfig
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler
import tm.alashow.datmusic.ui.downloader.DownloaderHost
import tm.alashow.datmusic.ui.downloads.audio.LocalAudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloads.audio.audioDownloadItemActionHandler
import tm.alashow.datmusic.ui.home.Home
import tm.alashow.datmusic.ui.playback.PlaybackHost
import tm.alashow.datmusic.ui.settings.LocalAppVersion
import tm.alashow.datmusic.ui.snackbar.SnackbarMessagesListener
import tm.alashow.navigation.NavigatorHost
import tm.alashow.navigation.rememberBottomSheetNavigator
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun DatmusicApp(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberAnimatedNavController(),
    analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(LocalContext.current),
) {
    CompositionLocalProvider(
        LocalScaffoldState provides scaffoldState,
        LocalAnalytics provides analytics,
        LocalAppVersion provides BuildConfig.VERSION_NAME
    ) {
        ProvideWindowInsets(consumeWindowInsets = false) {
            DatmusicCore {
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                navController.navigatorProvider += bottomSheetNavigator
                ModalBottomSheetLayout(bottomSheetNavigator) {
                    Home(navController)
                }
            }
        }
    }
}

@Composable
private fun DatmusicCore(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    SnackbarMessagesListener()
    val themeState by rememberFlowWithLifecycle(themeViewModel.themeState)
    AppTheme(themeState) {
        NavigatorHost {
            DownloaderHost {
                PlaybackHost {
                    DatmusicActionHandlers {
                        content()
                    }
                }
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
