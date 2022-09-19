/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import android.content.Context
import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.ui.detail.MediaDetailViewState
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@Immutable
internal data class AlbumDetailViewState(
    val album: Album? = null,
    val albumDetails: Async<Audios> = Uninitialized
) : MediaDetailViewState<Audios> {

    override val isLoaded = album != null
    override val isEmpty = albumDetails is Success && albumDetails.invoke().isEmpty()
    override val title = album?.title

    override fun artwork(context: Context) = album?.photo?.mediumUrl
    override fun details() = albumDetails

    companion object {
        val Empty = AlbumDetailViewState()
    }
}
