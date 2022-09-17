/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.imageloading.getBitmap
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao
import tm.alashow.datmusic.data.repos.audio.AudioSaveType
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.ArtworkImageFileType.Companion.isUserSetArtworkPath
import tm.alashow.datmusic.data.repos.playlist.PlaylistArtworkUtils.savePlaylistArtwork
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.domain.entities.AudioId
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistAudioIds
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.domain.entities.PlaylistWithItems
import tm.alashow.i18n.DatabaseInsertError
import tm.alashow.i18n.DatabaseNotFoundError
import tm.alashow.i18n.ValidationErrorBlank
import tm.alashow.i18n.ValidationErrorTooLong

class PlaylistsRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val dao: PlaylistsDao,
    private val playlistAudiosDao: PlaylistsWithAudiosDao,
    private val audiosRepo: AudiosRepo,
) : RoomRepo<PlaylistId, Playlist>(dao, dispatchers), CoroutineScope by ProcessLifecycleOwner.get().lifecycleScope {

    suspend fun getByName(name: String): Playlist? = dao.getByName(name)
    fun playlist(id: PlaylistId): Flow<Playlist> = entryNotNull(id)

    fun playlistItems(id: PlaylistId) = playlistAudiosDao.playlistItems(id)
    fun playlistWithItems(id: PlaylistId) = combine(playlist(id), playlistItems(id), ::PlaylistWithItems)

    fun playlists(): Flow<List<Playlist>> = dao.entries()

    suspend fun validatePlaylistId(playlistId: PlaylistId) {
        if (!exists(playlistId)) {
            Timber.e("Playlist with id: $playlistId doesn't exist")
            throw DatabaseNotFoundError
        }
    }

    private fun validatePlaylist(playlist: Playlist) {
        if (playlist.name.isBlank()) {
            throw ValidationErrorBlank()
        }
        if (playlist.name.length > PLAYLIST_NAME_MAX_LENGTH) {
            throw ValidationErrorTooLong()
        }
    }

    suspend fun createPlaylist(playlist: Playlist, audioIds: AudioIds = emptyList()): PlaylistId {
        validatePlaylist(playlist)

        var playlistId: PlaylistId
        withContext(dispatchers.io) {
            playlistId = insert(playlist)
            if (playlistId < 0)
                throw DatabaseInsertError

            if (audioIds.isNotEmpty()) {
                addAudiosToPlaylist(playlistId, audioIds)
            }
        }

        return playlistId
    }

    suspend fun getOrCreatePlaylist(name: String, audioIds: AudioIds = emptyList(), ignoreExistingAudios: Boolean = true): PlaylistId {
        val existingPlaylist = getByName(name)
        val playlistId = existingPlaylist?.id ?: createPlaylist(Playlist(name = name))
        if (audioIds.isNotEmpty()) {
            withContext(dispatchers.io) {
                addAudiosToPlaylist(playlistId, audioIds, ignoreExisting = ignoreExistingAudios)
            }
        }
        return playlistId
    }

    suspend fun updatePlaylist(playlist: Playlist): Playlist {
        validatePlaylistId(playlist.id)
        validatePlaylist(playlist)
        return update(playlist.updatedCopy())
    }

    suspend fun updatePlaylist(playlistId: PlaylistId, applyUpdate: (Playlist) -> Playlist): Playlist {
        validatePlaylistId(playlistId)
        return updatePlaylist(applyUpdate(playlist(playlistId).first()))
    }

    suspend fun addAudiosToPlaylist(playlistId: PlaylistId, audioIds: AudioIds, ignoreExisting: Boolean = false): List<PlaylistAudioId> {
        val insertedIds = mutableListOf<PlaylistId>()
        val ignoredAudioIds = mutableListOf<AudioId>()
        validatePlaylistId(playlistId)

        withContext(dispatchers.io) {
            if (audioIds.isEmpty()) return@withContext
            ignoredAudioIds += audiosRepo.findMissingIds(audioIds)

            if (ignoreExisting) {
                ignoredAudioIds += playlistItems(playlistId).first().map { it.audio.id }.toSet()
            }

            val savedCount = audiosRepo.saveAudiosById(AudioSaveType.Playlist, audioIds)
            if (savedCount < audioIds.size) {
                Timber.e("Some audios are missing from database: $audioIds")
            }

            val lastIndex = playlistAudiosDao.lastPlaylistAudioPosition(playlistId) ?: -1
            val playlistAudios = audioIds
                .filterNot { ignoredAudioIds.contains(it) }
                .mapIndexed { index, id ->
                    PlaylistAudio(
                        playlistId = playlistId,
                        audioId = id,
                        position = lastIndex + (index + 1)
                    )
                }
            insertedIds += playlistAudiosDao.insertAll(playlistAudios)
            generatePlaylistArtwork(playlistId)
            return@withContext
        }
        return insertedIds
    }

    suspend fun swapPositions(playlistId: PlaylistId, from: Int, to: Int) {
        withContext(dispatchers.io) {
            validatePlaylistId(playlistId)

            val fromAudio = playlistAudiosDao.getByPosition(playlistId, from) ?: throw DatabaseNotFoundError
            val toAudio = playlistAudiosDao.getByPosition(playlistId, to) ?: throw DatabaseNotFoundError

            playlistAudiosDao.updatePosition(fromAudio.id, toPosition = to)
            playlistAudiosDao.updatePosition(toAudio.id, toPosition = from)
            generatePlaylistArtwork(playlistId)
        }
    }

    suspend fun updatePlaylistItems(playlistItems: PlaylistItems) {
        if (playlistItems.isEmpty()) {
            return
        }
        playlistAudiosDao.updateAll(playlistItems.map { it.playlistAudio })
        if (playlistItems.isNotEmpty()) {
            generatePlaylistsArtwork(playlistItems.map { it.playlistAudio.playlistId })
        }
    }

    suspend fun removePlaylistItems(ids: PlaylistAudioIds): Int {
        if (ids.isEmpty()) return 0
        val result = playlistAudiosDao.deletePlaylistItems(ids)
        generatePlaylistsArtwork(playlistAudiosDao.getByIds(ids).map { it.playlistId })
        return result
    }

    suspend fun clearPlaylistArtwork(playlistId: PlaylistId) {
        updatePlaylist(playlistId) {
            it.copy(artworkPath = null, artworkSource = null)
        }
        generatePlaylistArtwork(playlistId)
    }

    override suspend fun delete(id: PlaylistId): Int {
        validatePlaylistId(id)
        playlist(id).first().artworkFile()?.delete()
        return super.delete(id)
    }

    override suspend fun deleteAll(): Int {
        clearArtworksFolder()
        return super.deleteAll()
    }

    private fun clearArtworksFolder() {
        ArtworkImageFolderType.PLAYLIST.getArtworkImageFolder(context).delete()
    }

    suspend fun regeneratePlaylistArtworks() {
        for (playlist in playlists().first()) {
            generatePlaylistArtwork(playlist.id)
        }
    }

    private fun generatePlaylistsArtwork(playlistIds: List<PlaylistId>) {
        playlistIds.toSet().forEach { generatePlaylistArtwork(it) }
    }

    private fun generatePlaylistArtwork(playlistId: PlaylistId, maxArtworksNeeded: Int = 4) {
        launch(dispatchers.computation) {
            validatePlaylistId(playlistId)
            val playlist = playlist(playlistId).first().updatedCopy()
            val audiosOfPlaylist = playlistItems(playlistId).first()

            if (playlist.artworkPath.isUserSetArtworkPath()) {
                Timber.i("Skipping generating artwork for id=$playlistId because it has non-auto generated artwork")
                return@launch
            }

            if (audiosOfPlaylist.isNotEmpty()) {
                Timber.i("Considering to auto generate artwork for playlist id=$playlistId")
                val artworkUrls = audiosOfPlaylist.map { it.audio.coverUri(CoverImageSize.LARGE, allowAlternate = false) }
                    .filter { it.toString().isNotBlank() }
                    .toSet()
                    .take(maxArtworksNeeded)

                val artworksHash = artworkUrls.hashCode().toString()

                if (artworksHash == playlist.artworkSource) {
                    Timber.i("Skipping generating artwork for id=$playlistId because it has the same hash = $artworksHash")
                    return@launch
                }

                val artworkBitmaps = artworkUrls.mapNotNull { context.getBitmap(it, allowHardware = false) }

                if (artworkBitmaps.isNotEmpty()) {
                    playlist.artworkFile()?.delete()
                    val merged = PlaylistArtworkUtils.joinImages(artworkBitmaps)
                    val file = playlistId.savePlaylistArtwork(context, merged, ArtworkImageFileType.PLAYLIST_AUTO_GENERATED)

                    Timber.d("Auto-generated artwork for playlist: hash=$artworksHash, file=$file, from urls=${artworkUrls.joinToString()}")

                    val updatedPlaylist = playlist.copy(artworkPath = file.path, artworkSource = artworksHash)
                    updatePlaylist(updatedPlaylist)
                } else {
                    Timber.w("Playlist id=$playlistId doesn't have any audios with artwork")
                }
            } else {
                Timber.w("Playlist id=$playlistId is empty, cannot generate artwork")
            }
        }
    }
}
