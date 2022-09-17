/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.i18n.ValidationError

@Immutable
internal data class EditPlaylistViewState(
    val name: String = "",
    val nameError: ValidationError? = null,
    val playlist: Playlist = Playlist(),
    val lastRemovedPlaylistItem: RemovedFromPlaylist? = null,
) {
    companion object {
        val Empty = EditPlaylistViewState()
    }
}

fun interface OnMovePlaylistItem {
    operator fun invoke(from: Int, to: Int)
}
