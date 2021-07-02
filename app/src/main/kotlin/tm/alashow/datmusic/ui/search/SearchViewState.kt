/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import tm.alashow.datmusic.data.repos.search.BackendTypes
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams

data class SearchFilter(
    val backends: BackendTypes = DefaultBackends
) {

    companion object {
        val DefaultBackends: BackendTypes = DatmusicSearchParams.BackendType.values().toSet()
    }
}

data class SearchViewState(
    val query: String = "",
    val searchFilter: SearchFilter = SearchFilter(),
    val error: Throwable? = null
) {
    companion object {
        val Empty = SearchViewState()
    }
}
