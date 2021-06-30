/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams

internal sealed class SearchAction {
    data class Search(val query: String = "") : SearchAction()
    data class SelectBackendType(val selected: Boolean, val backendType: DatmusicSearchParams.BackendType) : SearchAction()
}
