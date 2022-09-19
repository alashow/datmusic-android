/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import tm.alashow.base.util.Analytics
import tm.alashow.data.PreferencesStore

open class ResizableLayoutViewModel @Inject constructor(
    preferencesStore: PreferencesStore,
    preferenceKey: Preferences.Key<Float>,
    defaultDragOffset: Float = 0f,
    private val analyticsPrefix: String,
    private val analytics: Analytics,
) : ViewModel() {

    private val dragOffsetState = preferencesStore.getStateFlow(
        preferenceKey, viewModelScope, defaultDragOffset,
        saveDebounce = 200
    )
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
            dragOffsetState
                .debounce(2000)
                .collectLatest { analytics.event("$analyticsPrefix.resize", mapOf("offset" to it)) }
        }
    }
}
