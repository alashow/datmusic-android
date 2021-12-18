/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import tm.alashow.data.PreferencesStore

private val HomeNavigationRailDragOffsetKey = floatPreferencesKey("HomeNavigationRailWeightKey")
private const val HomeNavigationRailWeightDefault = 0f

@HiltViewModel
class ResizableHomeNavigationRailViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val preferencesStore: PreferencesStore,
) : ViewModel() {

    private val dragOffsetState = MutableStateFlow(handle.get(HomeNavigationRailDragOffsetKey.name) ?: HomeNavigationRailWeightDefault)
    val dragOffset = dragOffsetState.asStateFlow()

    init {
        persistDragOffset()
    }

    fun setDragOffset(newOffset: Float) {
        viewModelScope.launch {
            dragOffsetState.value = newOffset
        }
    }

    private fun persistDragOffset() {
        viewModelScope.launch {
            launch {
                preferencesStore.get(HomeNavigationRailDragOffsetKey, HomeNavigationRailWeightDefault).collectLatest {
                    dragOffsetState.value = it
                }
            }
            launch {
                dragOffsetState
                    .debounce(1000)
                    .collectLatest {
                        preferencesStore.save(HomeNavigationRailDragOffsetKey, it)
                    }
            }
        }
    }
}
