/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import android.content.Context
import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.ui.detail.MediaDetailViewState
import tm.alashow.datmusic.ui.utils.AudiosCountDuration
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@Immutable
internal data class PlaylistDetailViewState(
    val playlist: Playlist? = null,
    val playlistDetails: Async<PlaylistItems> = Uninitialized,
    val params: ObservePlaylistDetails.Params = ObservePlaylistDetails.Params(),
    val audiosCountDuration: AudiosCountDuration? = null,
) : MediaDetailViewState<PlaylistItems> {

    override val isLoaded = playlist != null
    override val isEmpty = playlistDetails is Success && playlistDetails.invoke().isEmpty()
    override val title = playlist?.name

    override fun artwork(context: Context) = playlist?.artworkFile()
    override fun details() = playlistDetails

    companion object {
        val Empty = PlaylistDetailViewState()
    }
}
