/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import android.content.Context
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios
import tm.alashow.datmusic.ui.detail.MediaDetailViewState
import tm.alashow.datmusic.ui.utils.AudiosCountDuration
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

typealias DragMove = Pair<Int, Int>

data class PlaylistDetailViewState(
    val playlist: Playlist? = null,
    val playlistDetails: Async<PlaylistWithAudios> = Uninitialized,
    val dragMove: DragMove? = null,
    val audiosCountDuration: AudiosCountDuration? = null,
) : MediaDetailViewState<PlaylistWithAudios> {

    override val isLoaded = playlist != null
    override val isEmpty = playlistDetails is Success && playlistDetails.invoke().audios.isEmpty()
    override val title = playlist?.name

    override fun artwork(context: Context) = playlist?.artworkFile()
    override fun details() = playlistDetails

    companion object {
        val Empty = PlaylistDetailViewState()
    }
}
