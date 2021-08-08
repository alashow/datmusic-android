/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Uninitialized

data class ArtistDetailViewState(
    val artist: Artist? = null,
    val artistDetails: Async<Artist> = Uninitialized
) {
    companion object {
        val Empty = ArtistDetailViewState()
    }
}
