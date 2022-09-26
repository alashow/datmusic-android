/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import android.content.Context
import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.ui.detail.MediaDetailViewState
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@Immutable
internal data class ArtistDetailViewState(
    val artist: Artist? = null,
    val artistDetails: Async<Artist> = Uninitialized
) : MediaDetailViewState<Artist> {

    override val isLoaded = artist != null
    override val isEmpty = artistDetails is Success && artistDetails.invoke().audios.isEmpty()
    override val title = artist?.name

    override fun artwork(context: Context) = artist?.largePhoto()
    override fun details() = artistDetails

    companion object {
        val Empty = ArtistDetailViewState()
    }
}
