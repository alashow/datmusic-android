/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.datastore.preferences.core.floatPreferencesKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.base.ui.base.vm.ResizableLayoutViewModel
import tm.alashow.base.util.Analytics
import tm.alashow.data.PreferencesStore

private val PlaybackSheetLayoutDragOffsetKey = floatPreferencesKey("PlaybackSheetLayoutDragOffsetKey")

@HiltViewModel
class ResizablePlaybackSheetLayoutViewModel @Inject constructor(
    preferencesStore: PreferencesStore,
    analytics: Analytics,
) : ResizableLayoutViewModel(
    preferencesStore = preferencesStore,
    analytics = analytics,
    preferenceKey = PlaybackSheetLayoutDragOffsetKey,
    defaultDragOffset = 0f,
    analyticsPrefix = "playbackSheet.layout"
)
