/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

data class SearchViewState(
    val query: String = "",
) {
    companion object {
        val Empty = SearchViewState()
    }
}
