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
import tm.alashow.datmusic.domain.entities.Playlists
import tm.alashow.datmusic.domain.entities.PlaylistsWithAudios
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Params
import tm.alashow.domain.models.Success

class ObservePlaylists @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<Params, Playlists>() {
    override fun createObservable(params: Params): Flow<Playlists> = playlistsRepo.playlists()
}

class ObservePlaylistsDetails @Inject constructor(
    private val playlistsRepo: PlaylistsRepo
) : SubjectInteractor<Params, Async<PlaylistsWithAudios>>() {

    override fun createObservable(params: Params) = flow {
        emit(Loading())
        playlistsRepo.playlistsWithAudios()
            .catch { error -> emit(Fail<PlaylistsWithAudios>(error)) }
            .collect { emit(Success(it)) }
    }
}
