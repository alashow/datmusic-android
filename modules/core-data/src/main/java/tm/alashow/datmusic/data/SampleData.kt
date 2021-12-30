/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.nextInt
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio

object SampleData {
    private val random = Random(1000)

    fun Random.id(): Long = abs(nextLong())
    fun Random.sid(): String = nextInt().toString()

    fun randomString() = UUID.randomUUID().toString().replace("-", "")

    val Audio: Audio = audio()
    val Playlist: Playlist = playlist()
    val PlaylistAudio: PlaylistAudio = playlistAudioItems(Playlist, Audio).playlistAudio

    val Artist: Artist = artist()
    val Album: Album = album(Artist)

    fun audio() = Audio(
        id = "sample-audio-${random.id()}",
        primaryKey = "primary-sample-audio-${random.id()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        params = random.nextInt().toString(),
        artist = "Artist ${randomString()}",
        title = "Title ${randomString()}",
        album = "Album ${randomString()}",
        downloadUrl = "https://test.com/test-download.mp3",
        streamUrl = "https://test.com/test-stream.mp3",
        duration = random.nextInt(100, 300)
    )

    fun playlist() = Playlist(id = random.id(), name = "Playlist ${random.id()}")

    data class PlaylistAudioItem(val playlist: Playlist, val audio: Audio, val playlistAudio: PlaylistAudio)

    fun playlistAudioItems(playlist: Playlist = playlist(), audio: Audio = audio()) =
        PlaylistAudioItem(playlist, audio, PlaylistAudio(id = random.nextLong(), playlistId = playlist.id, audioId = audio.id))

    fun album(mainArtist: Artist = artist()) = Album(
        id = random.sid(),
        primaryKey = "sample-album-${random.id()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        artistId = random.id(),
        title = "Album ${random.id()}",
        year = random.nextInt(1900, 2030),
        songCount = random.nextInt(1, 10),
        explicit = random.nextBoolean(),
        artists = listOf(mainArtist)
    )

    fun artist() = Artist(
        id = random.sid(),
        primaryKey = "sample-artist-${random.id()}",
        searchIndex = random.nextInt(),
        page = random.nextInt(),
        name = "Artist ${random.id()}"
    )

    fun downloadRequest(audio: Audio = audio()) = DownloadRequest.fromAudio(audio.copy(id = "${audio.id}-downloaded"))
}
