/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MainViewState(
    val response: String = "",
    val error: Throwable = Throwable()
) {
    companion object {
        val Empty = MainViewState()
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(val handle: SavedStateHandle) : ViewModel() {

    private val responseState = MutableStateFlow("")
    private val responseError = MutableStateFlow(Throwable())

    val state = combine(responseState, responseError) { response, error ->
        MainViewState(response, error)
    }

    init {
        viewModelScope.launch {
        }
    }
}
