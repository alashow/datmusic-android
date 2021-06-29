/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.home.Home
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.DefaultTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val appViewModel = viewModel<AppViewModel>()
            val themeState by rememberFlowWithLifecycle(appViewModel.themeState).collectAsState(DefaultTheme)

            AppTheme(themeState) {
                ProvideWindowInsets(consumeWindowInsets = false) {
                    Home()
                }
            }
        }
    }
}
