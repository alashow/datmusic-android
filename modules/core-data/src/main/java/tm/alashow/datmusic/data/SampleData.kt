/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.EnqueueAction
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadInfo
import com.tonyodev.fetch2core.Extras
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.random.Random
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistItem

/**
 * TODO: Enable random chaos testing in dev environment only / disable in CI.
 * Deterministic tests are required in CI for reproducibility.
 * @see <a href="https://en.wikipedia.org/wiki/Random_testing">Random testing</a>
 */
private const val RANDOM_CHAOS_TESTING_ENABLED = false
val Randomness = Random(if (RANDOM_CHAOS_TESTING_ENABLED) SecureRandom().nextInt() else 9999)

private fun id() = abs(Randomness.nextLong())
private fun sid() = Randomness.nextInt().toString()

object SampleData {
    fun <T> list(n: Int = 100, creator: SampleData.() -> T): List<T> = buildList { repeat(n) { add(creator(SampleData)) } }

    val Audio: Audio = audio()
    val Playlist: Playlist = playlist()

    val Artist: Artist = artist()
    val Album: Album = album(Artist)

    fun audio() = Audio(
        id = "sample-audio-${id()}",
        primaryKey = "primary-sample-audio-${id()}",
        searchIndex = Randomness.nextInt(),
        page = Randomness.nextInt(),
        params = Randomness.nextInt().toString(),
        artist = "Artist ${id()}",
        title = "Title ${id()}",
        album = "Album ${id()}",
        coverUrl = "https://picsum.photos/seed/${id()}/600/600",
        downloadUrl = "https://test.com/test-download.mp3",
        streamUrl = "https://test.com/test-stream.mp3",
        duration = Randomness.nextInt(100, 300)
    )

    fun playlist() = Playlist(id = id(), name = "Playlist ${id()}")

    data class PlaylistAudioItem(val playlist: Playlist, val audio: Audio, val playlistAudio: PlaylistAudio)

    fun playlistAudioItem(playlist: Playlist = playlist(), audio: Audio = audio()) =
        PlaylistAudioItem(playlist, audio, PlaylistAudio(id = Randomness.nextLong(), playlistId = playlist.id, audioId = audio.id))

    fun playlistItem(playlist: Playlist = playlist(), audio: Audio = audio()) =
        PlaylistItem(PlaylistAudio(id = Randomness.nextLong(), playlistId = playlist.id, audioId = audio.id), audio)

    fun album(mainArtist: Artist = artist()) = Album(
        id = sid(),
        primaryKey = "sample-album-${id()}",
        searchIndex = Randomness.nextInt(),
        page = Randomness.nextInt(),
        artistId = id(),
        title = "Album ${id()}",
        year = Randomness.nextInt(1900, 2030),
        songCount = Randomness.nextInt(1, 10),
        explicit = Randomness.nextBoolean(),
        artists = listOf(mainArtist)
    )

    fun artist() = Artist(
        id = sid(),
        primaryKey = "sample-artist-${id()}",
        searchIndex = Randomness.nextInt(),
        page = Randomness.nextInt(),
        name = "Artist ${id()}"
    )

    fun downloadRequest(audio: Audio = audio()) = DownloadRequest.fromAudio(audio.copy(id = "${audio.id}-downloaded"))

    fun downloadInfo(
        id: Int = Randomness.nextInt(),
        namespace: String = "sample-namespace-${id()}",
        url: String = "https://test.com/test-download.mp3",
        file: String = "test-download.mp3",
        group: Int = Randomness.nextInt(),
        priority: Priority = Priority.HIGH,
        headers: Map<String, String> = mapOf(),
        total: Long = Randomness.nextLong(2000000, 25000000), // 2-25MB
        downloaded: Long = total / 2,
        status: Status = Status.values().random(Randomness),
        error: Error = Error.values().random(Randomness),
        networkType: NetworkType = NetworkType.values().random(Randomness),
        created: Long = Randomness.nextLong(),
        tag: String? = "sample-tag-${id()}",
        enqueueAction: EnqueueAction = EnqueueAction.values().random(Randomness),
        identifier: Long = Randomness.nextLong(),
        downloadOnEnqueue: Boolean = Randomness.nextBoolean(),
        extras: Extras = Extras.emptyExtras,
        autoRetryMaxAttempts: Int = Randomness.nextInt(),
        autoRetryAttempts: Int = Randomness.nextInt(),
        etaInMilliSeconds: Long = Randomness.nextLong(),
        downloadedBytesPerSecond: Long = Randomness.nextLong()
    ): Download = DownloadInfo().apply {
        this.id = id
        this.namespace = namespace
        this.url = url
        this.file = file
        this.group = group
        this.priority = priority
        this.headers = headers
        this.downloaded = downloaded
        this.total = total
        this.status = status
        this.error = error
        this.networkType = networkType
        this.created = created
        this.tag = tag
        this.enqueueAction = enqueueAction
        this.identifier = identifier
        this.downloadOnEnqueue = downloadOnEnqueue
        this.extras = extras
        this.autoRetryMaxAttempts = autoRetryMaxAttempts
        this.autoRetryAttempts = autoRetryAttempts

        this.etaInMilliSeconds = etaInMilliSeconds
        this.downloadedBytesPerSecond = downloadedBytesPerSecond
    }

    fun audioDownloadItem(
        audio: Audio = audio(),
        download: Download = downloadInfo(),
        downloadRequest: DownloadRequest = downloadRequest(audio)
    ) = AudioDownloadItem(downloadRequest, download, audio)
}
