/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import tm.alashow.base.ui.SnackbarAction
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.i18n.UiMessage

data class AddedToPlaylistMessage(val playlist: Playlist) :
    SnackbarMessage<PlaylistId>(
        message = UiMessage.Resource(
            R.string.playlist_addTo_added,
            formatArgs = listOf(playlist.name)
        ),
        action = SnackbarAction(UiMessage.Resource(R.string.playlist_addTo_open), playlist.id)
    )
