/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tm.alashow.base.ui.ThemeState
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.data.PreferencesStore
import tm.alashow.ui.theme.DefaultTheme

object PreferenceKeys {
    const val THEME_STATE_KEY = "theme_state"
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferences: PreferencesStore,
    private val analytics: Analytics,
) : ViewModel() {

    // Read saved theme state from preferences in a blocking manner (takes ~5 ms)
    // so the app doesn't render first frames with the default theme
    private val savedThemeState = runBlocking {
        preferences.get(PreferenceKeys.THEME_STATE_KEY, ThemeState.serializer(), DefaultTheme).first()
    }
    val themeState = preferences.get(PreferenceKeys.THEME_STATE_KEY, ThemeState.serializer(), DefaultTheme)
        .stateInDefault(viewModelScope, savedThemeState)

    fun applyThemeState(themeState: ThemeState) {
        analytics.event("theme.apply", mapOf("darkMode" to themeState.isDarkMode, "palette" to themeState.colorPalettePreference.name))
        viewModelScope.launch {
            preferences.save(PreferenceKeys.THEME_STATE_KEY, themeState, ThemeState.serializer())
        }
    }
}
