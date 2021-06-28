/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

internal sealed class SearchAction {
    data class Search(val query: String = "") : SearchAction()
}
