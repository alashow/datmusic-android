/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library

import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.domain.entities.LibraryItems
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Uninitialized

@Immutable
internal data class LibraryViewState(
    val items: Async<LibraryItems> = Uninitialized,
) {
    companion object {
        val Empty = LibraryViewState()
    }
}
