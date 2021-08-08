/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.navigation.Navigator

@HiltViewModel
class HomeViewModel @Inject constructor(
    val navigator: Navigator,
    private val handle: SavedStateHandle,
) : ViewModel()
