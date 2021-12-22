/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import android.content.Context
import tm.alashow.domain.models.Async

interface MediaDetailViewState<DetailType> {
    val isLoading: Boolean get() = details().isLoading
    val isLoaded: Boolean get() = false
    val isEmpty: Boolean get() = false
    val title: String? get() = null

    fun artwork(context: Context): Any? = null
    fun details(): Async<DetailType>
}
