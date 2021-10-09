/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistId

class PlaylistsRepo @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val dao: PlaylistsDao,
    private val playlistAudiosDao: PlaylistsWithAudiosDao,
) : RoomRepo<Playlist>(dao, dispatchers) {

    suspend fun createPlaylist(playlist: Playlist, audioIds: List<String> = listOf()): PlaylistId? {
        var playlistId: PlaylistId?
        withContext(dispatchers.io) {
            playlistId = dao.insert(playlist)
            playlistId?.let { addAudiosToPlaylist(it, audioIds) }
        }

        return playlistId
    }

    suspend fun addAudiosToPlaylist(playlistId: PlaylistId, audioIds: List<String>): List<PlaylistId> {
        val insertedIds = mutableListOf<PlaylistId>()
        withContext(dispatchers.io) {
            if (dao.has(playlistId.toString()) <= 0) {
                Timber.e("Playlist with id: $playlistId doesn't exist")
                return@withContext
            }

            val lastIndex = playlistAudiosDao.lastPlaylistAudioIndex(playlistId).firstOrNull() ?: 0
            val playlistWithAudios = audioIds.mapIndexed { index, id ->
                PlaylistAudio(
                    playlistId = playlistId,
                    audioId = id,
                    position = lastIndex + (index + 1)
                )
            }
            insertedIds.addAll(playlistAudiosDao.insertAll(playlistWithAudios))
            return@withContext
        }
        return insertedIds
    }

    suspend fun swap(playlistId: PlaylistId, from: Int, to: Int) {
        withContext(dispatchers.io) {
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
}
