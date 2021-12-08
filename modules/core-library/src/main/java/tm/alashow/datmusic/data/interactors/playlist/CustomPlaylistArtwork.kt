/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.imageloading.getBitmap
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.repos.playlist.ArtworkImageFileType
import tm.alashow.datmusic.data.repos.playlist.PlaylistArtworkUtils.savePlaylistArtwork
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.i18n.LoadingError

class SetCustomPlaylistArtwork @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: PlaylistsRepo,
    private val dispatchers: CoroutineDispatchers,
) : ResultInteractor<SetCustomPlaylistArtwork.Params, Unit>() {

    data class Params(val playlistId: PlaylistId, val uri: Uri)

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        repo.validatePlaylistId(params.playlistId)
        val bitmap = appContext.getBitmap(params.uri, allowHardware = false) ?: throw LoadingError
        val artwork = params.playlistId.savePlaylistArtwork(appContext, bitmap, ArtworkImageFileType.PLAYLIST_USER_SET, recycle = false)

        val playlist = repo.playlist(params.playlistId).first()
        repo.updatePlaylist(playlist.copy(artworkPath = artwork.path, artworkSource = artwork.path.hashCode().toString()))
        return@withContext
    }
}

class ClearPlaylistArtwork @Inject constructor(
    private val repo: PlaylistsRepo,
    private val dispatchers: CoroutineDispatchers,
) : ResultInteractor<PlaylistId, Unit>() {

    override suspend fun doWork(params: PlaylistId) = withContext(dispatchers.io) {
        repo.clearPlaylistArtwork(params)
        return@withContext
    }
}
