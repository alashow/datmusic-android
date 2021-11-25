/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import kotlin.random.Random
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio

internal object SampleData {
    private val random = Random(1000)

    val Audio: Audio = audio()
    val Playlist: Playlist = playlist()
    val PlaylistAudio: PlaylistAudio = playlistAudioItems(Playlist, Audio).playlistAudio

    val Artist: Artist = artist()
    val Album: Album = album(Artist)

    fun audio() = Audio(
        id = "sample-audio-${random.nextInt()}",
        primaryKey = "sample-audio-${random.nextInt()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        params = random.nextInt().toString(),
        artist = "Artist ${random.nextInt()}",
        title = "Title ${random.nextInt()}",
        duration = random.nextInt(100, 300)
    )

    fun playlist() = Playlist(id = random.nextLong(), name = "Playlist ${random.nextInt()}")

    data class PlaylistAudioItem(val playlist: Playlist, val audio: Audio, val playlistAudio: PlaylistAudio)

    fun playlistAudioItems(playlist: Playlist = playlist(), audio: Audio = audio()) =
        PlaylistAudioItem(playlist, audio, PlaylistAudio(id = random.nextLong(), playlistId = playlist.id, audioId = audio.id))

    fun album(mainArtist: Artist = artist()) = Album(
        id = "100",
        primaryKey = "sample-album-${random.nextInt()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        artistId = 100,
        title = "Album ${random.nextInt()}",
        year = random.nextInt(1900, 2030),
        songCount = random.nextInt(1, 10),
        explicit = random.nextBoolean(),
        artists = listOf(mainArtist)
    )

    fun artist() = Artist(
        id = random.nextInt().toString(),
        primaryKey = "sample-artist-${random.nextInt()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        name = "Artist ${random.nextInt()}"
    )
}
