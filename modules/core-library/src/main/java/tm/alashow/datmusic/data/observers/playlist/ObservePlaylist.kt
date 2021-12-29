/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItems

class ObservePlaylist @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Playlist>() {
    override fun createObservable(params: PlaylistId): Flow<Playlist> = playlistsRepo.playlist(params)
}

class ObservePlaylistExistence @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Boolean>() {
    override fun createObservable(params: PlaylistId): Flow<Boolean> = playlistsRepo.has(params)
}

class ObservePlaylistDetails @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, PlaylistItems>() {
    override fun createObservable(params: PlaylistId) = playlistsRepo
        .playlistItems(params)
}
