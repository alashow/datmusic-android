/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import tm.alashow.base.ui.ThemeState
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.data.PreferencesStore
import tm.alashow.ui.theme.DefaultTheme

object PreferenceKeys {
    const val THEME_STATE_KEY = "theme_state"
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val preferences: PreferencesStore,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    val themeState = preferences.get(PreferenceKeys.THEME_STATE_KEY, ThemeState.serializer(), DefaultTheme)
        .stateInDefault(viewModelScope, DefaultTheme)

    fun applyThemeState(themeState: ThemeState) {
        analytics.event("theme.apply", mapOf("darkMode" to themeState.isDarkMode, "palette" to themeState.colorPalettePreference.name))
        viewModelScope.launch {
            preferences.save(PreferenceKeys.THEME_STATE_KEY, themeState, ThemeState.serializer())
        }
    }
}
