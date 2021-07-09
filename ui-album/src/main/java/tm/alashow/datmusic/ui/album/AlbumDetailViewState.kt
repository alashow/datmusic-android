/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Uninitialized

data class AlbumDetailViewState(
    val album: Album? = null,
    val albumDetails: Async<List<Audio>> = Uninitialized
) {
    companion object {
        val Empty = AlbumDetailViewState()
    }
}
