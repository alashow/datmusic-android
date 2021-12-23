/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import tm.alashow.base.util.event
import tm.alashow.data.PreferencesStore

open class ResizableLayoutViewModel @Inject constructor(
    private val preferencesStore: PreferencesStore,
    private val analytics: FirebaseAnalytics,
    private val preferenceKey: Preferences.Key<Float>,
    private val defaultDragOffset: Float = 0f,
    private val analyticsPrefix: String
) : ViewModel() {

    private val dragOffsetState = MutableStateFlow(defaultDragOffset)
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
            preferencesStore.get(preferenceKey, defaultDragOffset)
                .collectLatest { dragOffsetState.value = it }
        }
        viewModelScope.launch {
            dragOffsetState
                .debounce(200)
                .collectLatest { preferencesStore.save(preferenceKey, it) }
        }
        viewModelScope.launch {
            dragOffsetState
                .debounce(2000)
                .collectLatest { analytics.event("$analyticsPrefix.resize", mapOf("offset" to it)) }
        }
    }
}
