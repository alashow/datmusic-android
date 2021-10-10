/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.imageloading.getBitmap
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao
import tm.alashow.datmusic.data.repos.playlist.PlaylistArtworkUtils.savePlaylistArtwork
import tm.alashow.datmusic.domain.entities.CoverImageSize
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.i18n.DatabaseInsertError
import tm.alashow.i18n.DatabaseValidationNotFound
import tm.alashow.i18n.ValidationErrorBlank
import tm.alashow.i18n.ValidationErrorTooLong

class PlaylistsRepo @Inject constructor(
    private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val dao: PlaylistsDao,
    private val playlistAudiosDao: PlaylistsWithAudiosDao,
) : RoomRepo<PlaylistId, Playlist>(dao, dispatchers), CoroutineScope by ProcessLifecycleOwner.get().lifecycleScope {

    private suspend fun validatePlaylistId(playlistId: PlaylistId) {
        if (!exists(playlistId)) {
            Timber.e("Playlist with id: $playlistId doesn't exist")
            throw DatabaseValidationNotFound.error()
        }
    }

    private fun validatePlaylist(playlist: Playlist) {
        if (playlist.name.isBlank()) {
            throw ValidationErrorBlank().error()
        }
        if (playlist.name.length > PLAYLIST_NAME_MAX_LENGTH) {
            throw ValidationErrorTooLong().error()
        }
    }

    suspend fun createPlaylist(playlist: Playlist, audioIds: List<String> = listOf()): PlaylistId {
        validatePlaylist(playlist)

        var playlistId: PlaylistId
        withContext(dispatchers.io) {
            playlistId = dao.insert(playlist)
            if (playlistId < 0)
                throw DatabaseInsertError.error()

            addAudiosToPlaylist(playlistId, audioIds)
        }

        return playlistId
    }

    suspend fun updatePlaylist(playlist: Playlist): Playlist {
        validatePlaylist(playlist)
        val updatedPlaylist: Playlist
        withContext(dispatchers.io) {
            updatedPlaylist = update(playlist)
        }

        return updatedPlaylist
    }

    suspend fun addAudiosToPlaylist(playlistId: PlaylistId, audioIds: List<String>): List<PlaylistId> {
        val insertedIds = mutableListOf<PlaylistId>()
        withContext(dispatchers.io) {
            validatePlaylistId(playlistId)

            val lastIndex = playlistAudiosDao.lastPlaylistAudioIndex(playlistId).firstOrNull() ?: 0
            val playlistWithAudios = audioIds.mapIndexed { index, id ->
                PlaylistAudio(
                    playlistId = playlistId,
                    audioId = id,
                    position = lastIndex + (index + 1)
                )
            }
            insertedIds.addAll(playlistAudiosDao.insertAll(playlistWithAudios))
            generatePlaylistArtwork(playlistId)
            return@withContext
        }
        return insertedIds
    }

    suspend fun swap(playlistId: PlaylistId, from: Int, to: Int) {
        withContext(dispatchers.io) {
            validatePlaylistId(playlistId)

            val playlistAudios = playlistAudiosDao.playlistAudios(playlistId).first()
            val fromId = playlistAudios.first { it.position == from }.audioId
            val toId = playlistAudios.first { it.position == to }.audioId

            playlistAudiosDao.updatePlaylistAudio(
                PlaylistAudio(
                    playlistId = playlistId,
                    audioId = fromId,
                    position = to
                )
            )
            playlistAudiosDao.updatePlaylistAudio(
                PlaylistAudio(
                    playlistId = playlistId,
                    audioId = toId,
                    position = from
                )
            )
        }
    }

    fun playlists() = dao.entries()
    fun playlist(id: PlaylistId) = dao.entry(id)
    fun playlistsWithAudios() = playlistAudiosDao.playlistsWithAudios()
    fun playlistWithAudios(id: PlaylistId) = playlistAudiosDao.playlistWithAudios(id)

    private fun generatePlaylistArtwork(playlistId: PlaylistId, maxArtworksNeeded: Int = 4) {
        launch(dispatchers.computation) {
            validatePlaylistId(playlistId)
            val playlistWithAudios = playlistWithAudios(playlistId).first()
            val playlistAudios = playlistWithAudios.audios

            if (playlistAudios.isNotEmpty()) {
                Timber.i("Generating artwork for playlist id=$playlistId")
                val artworkUrls = playlistAudios.map { it.coverUri(CoverImageSize.LARGE) }
                    .filter { it.toString().isNotBlank() }
                    .toSet()
                    .take(maxArtworksNeeded)
                val artworkBitmaps = artworkUrls.mapNotNull { context.getBitmap(it, allowHardware = false) }

                if (artworkBitmaps.isNotEmpty()) {
                    val merged = PlaylistArtworkUtils.joinImages(artworkBitmaps)
                    playlistId.savePlaylistArtwork(context, merged)
                } else {
                    Timber.w("Playlist id=$playlistId doesn't have any audios with artwork")
                }
            } else {
                Timber.w("Playlist id=$playlistId is empty, cannot generate artwork")
            }
        }
    }
}
