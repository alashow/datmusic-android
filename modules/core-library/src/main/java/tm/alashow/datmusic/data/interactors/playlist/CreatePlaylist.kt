/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import android.content.Context
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.R
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.i18n.ValidationErrorBlank

class CreatePlaylist @Inject constructor(
    private val context: Context,
    private val repo: PlaylistsRepo,
    private val dispatchers: CoroutineDispatchers,
) : ResultInteractor<CreatePlaylist.Params, Playlist>() {

    data class Params(val name: String = "", val generateNameIfEmpty: Boolean = false, var audios: Audios = emptyList())

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        var name = params.name

        if (name.isBlank()) {
            if (params.generateNameIfEmpty) {
                val playlistCount = repo.count().first() + 1
                name = context.getString(R.string.playlist_create_generatedTemplate, playlistCount)
            } else throw ValidationErrorBlank().error()
        }

        val playlistId = repo.createPlaylist(Playlist(name = name))
        return@withContext repo.playlist(playlistId).first()
    }
}
