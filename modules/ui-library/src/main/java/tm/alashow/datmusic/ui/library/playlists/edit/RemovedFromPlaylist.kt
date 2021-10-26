/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import android.content.Context
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import tm.alashow.base.ui.SnackbarAction
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.base.util.asString
import tm.alashow.base.util.extensions.Callback
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.i18n.UiMessage

data class RemovedFromPlaylist(val playlistItem: PlaylistItem, val removedIndex: Int) :
    SnackbarMessage<PlaylistItem>(
        message = UiMessage.Resource(R.string.playlist_edit_removed),
        action = SnackbarAction(
            UiMessage.Resource(R.string.playlist_edit_removed_undo),
            playlistItem
        )
    ) {

    fun asSnackbar(context: Context, onUndo: Callback): SnackbarData {
        val messageString = message.asString(context)
        return object : SnackbarData {
            override val actionLabel = action?.label?.asString(context)
            override val duration = SnackbarDuration.Indefinite
            override val message = messageString

            override fun performAction() {
                onUndo()
            }

            override fun dismiss() {}
        }
    }

    companion object {
        const val SNACKBAR_DURATION_MILLIS = 10 * 1000L
    }
}
