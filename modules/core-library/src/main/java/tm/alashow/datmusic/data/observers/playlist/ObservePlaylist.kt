/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.AudiosOfPlaylist
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

class ObservePlaylistExistense @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Boolean>() {
    override fun createObservable(params: PlaylistId): Flow<Boolean> = playlistsRepo.has(params)
}

class ObservePlaylistDetails @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Async<AudiosOfPlaylist>>() {

    override fun createObservable(params: PlaylistId) = flow {
        emit(Loading())
        playlistsRepo.audiosOfPlaylist(params)
            .catch { error -> emit(Fail<AudiosOfPlaylist>(error)) }
            .collect { emit(Success(it)) }
    }
}

class ObservePlaylistItems @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<PlaylistId, Async<PlaylistItems>>() {

    override fun createObservable(params: PlaylistId) = flow {
        emit(Loading())
        playlistsRepo.playlistAudios(params)
            .catch { error -> emit(Fail<PlaylistItems>(error)) }
            .collect { emit(Success(it)) }
    }
}
