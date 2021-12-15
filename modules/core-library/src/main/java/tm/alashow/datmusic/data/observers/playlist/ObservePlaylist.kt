/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

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
) : SubjectInteractor<PlaylistId, Async<PlaylistItems>>() {

    override fun createObservable(params: PlaylistId) = channelFlow<Async<PlaylistItems>> {
        send(Loading())
        playlistsRepo.playlistItems(params)
            .catch { error -> send(Fail(error)) }
            .collectLatest { send(Success(it)) }
    }
}

class ObservePlaylistItems @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Async<PlaylistItems>>() {

    override fun createObservable(params: PlaylistId) = channelFlow<Async<PlaylistItems>> {
        send(Loading())
        playlistsRepo.playlistItems(params)
            .catch { error -> send(Fail(error)) }
            .collectLatest { send(Success(it)) }
    }
}
