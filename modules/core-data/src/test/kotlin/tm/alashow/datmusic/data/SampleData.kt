/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import kotlin.random.Random
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio

internal object SampleData {
    private val random = Random(1000)

    val Audio: Audio = audio()
    val Playlist: Playlist = playlist()
    val PlaylistAudio: PlaylistAudio = playlistAudioItems(Playlist, Audio).playlistAudio

    fun audio() = Audio(
        id = "sample-audio-${random.nextInt()}",
        artist = "Artist ${random.nextInt()}",
        title = "Title ${random.nextInt()}",
        duration = random.nextInt(100, 300)
    ).let { it.copy(primaryKey = it.id) }

    fun playlist() = Playlist(id = random.nextLong(), name = "Playlist ${random.nextInt()}")

    data class PlaylistAudioItem(val playlist: Playlist, val audio: Audio, val playlistAudio: PlaylistAudio)

    fun playlistAudioItems(playlist: Playlist = playlist(), audio: Audio = audio()) =
        PlaylistAudioItem(playlist, audio, PlaylistAudio(id = random.nextLong(), playlistId = playlist.id, audioId = audio.id))
}
