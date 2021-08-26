/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.util.PriorityTaskManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import timber.log.Timber

interface AudioPlayer {
    fun play(startAtPosition: Long? = null)
    fun setSource(uri: Uri, local: Boolean = false): Boolean
    fun prepare()
    fun seekTo(position: Long)
    fun duration(): Long
    fun isPrepared(): Boolean
    fun isBuffering(): Boolean
    fun isPlaying(): Boolean
    fun position(): Long
    fun bufferedPosition(): Long
    fun pause()
    fun stop()
    fun release()
    fun onPrepared(prepared: OnPrepared<AudioPlayer>)
    fun onError(error: OnError<AudioPlayer>)
    fun onBuffering(buffering: OnBuffering<AudioPlayer>)
    fun onIsPlaying(playing: OnIsPlaying<AudioPlayer>)
    fun onReady(ready: OnReady<AudioPlayer>)
    fun onCompletion(completion: OnCompletion<AudioPlayer>)
}

@Singleton
class AudioPlayerImpl @Inject constructor(
    @ApplicationContext internal val context: Context,
    @Named("player") private val okHttpClient: OkHttpClient,
) : AudioPlayer, Player.Listener {

    private var playerBase: ExoPlayer? = null
    private val player: ExoPlayer
        get() {
            if (playerBase == null) {
                playerBase = createPlayer(this)
            }
            return playerBase ?: throw IllegalStateException("Could not create an audio player")
        }

    private var isPrepared = false
    private var isBuffering = false
    private var onPrepared: OnPrepared<AudioPlayer> = {}
    private var onError: OnError<AudioPlayer> = {}
    private var onBuffering: OnBuffering<AudioPlayer> = {}
    private var onIsPlaying: OnIsPlaying<AudioPlayer> = { _, _ -> }
    private var onReady: OnReady<AudioPlayer> = {}
    private var onCompletion: OnCompletion<AudioPlayer> = {}

    override fun play(startAtPosition: Long?) {
        if (startAtPosition == null) {
            player.playWhenReady = true
            return
        }
        player.seekTo(startAtPosition)
        player.playWhenReady = true
    }

    override fun setSource(uri: Uri, local: Boolean): Boolean {
        Timber.d("Setting source: local=$local, uri=$uri")
        return try {
            if (local) player.setMediaItem(MediaItem.fromUri(uri), true)
            else {
                val mediaSource = ProgressiveMediaSource.Factory(OkHttpDataSource.Factory(okHttpClient))
                    .createMediaSource(MediaItem.fromUri(uri))
                player.setMediaSource(mediaSource, true)
            }
            true
        } catch (ex: Exception) {
            onError(this, ex)
            false
        }
    }

    override fun prepare() {
        player.prepare()
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun duration() = player.duration

    override fun isPrepared() = isPrepared

    override fun isBuffering() = isBuffering

    override fun isPlaying() = player.isPlaying

    override fun position() = player.currentPosition

    override fun bufferedPosition() = player.bufferedPosition

    override fun pause() {
        player.playWhenReady = false
    }

    override fun stop() {
        player.stop()
    }

    override fun release() {
        player.release()
    }

    override fun onPrepared(prepared: OnPrepared<AudioPlayer>) {
        this.onPrepared = prepared
    }

    override fun onError(error: OnError<AudioPlayer>) {
        this.onError = error
    }

    override fun onBuffering(buffering: OnBuffering<AudioPlayer>) {
        this.onBuffering = buffering
    }

    override fun onIsPlaying(playing: OnIsPlaying<AudioPlayer>) {
        this.onIsPlaying = playing
    }

    override fun onReady(ready: OnReady<AudioPlayer>) {
        this.onReady = ready
    }

    override fun onCompletion(completion: OnCompletion<AudioPlayer>) {
        this.onCompletion = completion
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            Player.STATE_IDLE -> isBuffering = false
            Player.STATE_BUFFERING -> {
                isBuffering = true
                onBuffering(this)
            }
            Player.STATE_READY -> {
                isBuffering = false
                onReady(this)
            }
            Player.STATE_ENDED -> {
                isBuffering = false
                onCompletion(this)
            }
            else -> Unit
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        onIsPlaying(isPlaying, false)
    }

    override fun onPlayerError(error: PlaybackException) {
        isPrepared = false
        onError(this, error)
    }

    private fun createPlayer(owner: AudioPlayerImpl): ExoPlayer {
        return SimpleExoPlayer.Builder(
            context,
            DefaultRenderersFactory(context).apply {
                setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
            }
        )
            .setLoadControl(object : DefaultLoadControl() {
                override fun onPrepared() {
                    isPrepared = true
                    onPrepared(owner)
                }
            })
            .build().apply {
                val attr = AudioAttributes.Builder().apply {
                    setContentType(C.CONTENT_TYPE_MUSIC)
                    setUsage(C.USAGE_MEDIA)
                }.build()

                setAudioAttributes(attr, false)
                setPriorityTaskManager(PriorityTaskManager())
                addListener(owner)
            }
    }
}
