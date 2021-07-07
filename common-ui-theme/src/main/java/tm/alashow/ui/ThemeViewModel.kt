/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import tm.alashow.base.preferences.PreferencesStore
import tm.alashow.base.ui.ThemeState
import tm.alashow.ui.theme.DefaultTheme

object PreferenceKeys {
    const val THEME_STATE_KEY = "theme_state"
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    val handle: SavedStateHandle,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    val themeState = preferencesStore.get(PreferenceKeys.THEME_STATE_KEY, ThemeState.serializer(), DefaultTheme)

    fun applyThemeState(themeState: ThemeState) {
        viewModelScope.launch {
            preferencesStore.save(PreferenceKeys.THEME_STATE_KEY, themeState, ThemeState.serializer())
        }
    }
}
