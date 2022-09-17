/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.data.REMOTE_CONFIG_FETCH_DELAY
import tm.alashow.data.RemoteConfig
import tm.alashow.datmusic.data.config.getSettingsLinks

@HiltViewModel
internal class SettingsViewModel @Inject constructor(remoteConfig: RemoteConfig) : ViewModel() {

    val settingsLinks = flow {
        // initially fetch once then one more time when there might be an update
        emit(remoteConfig.getSettingsLinks())
        delay(REMOTE_CONFIG_FETCH_DELAY)
        emit(remoteConfig.getSettingsLinks())
    }.stateInDefault(viewModelScope, emptyList())
}
