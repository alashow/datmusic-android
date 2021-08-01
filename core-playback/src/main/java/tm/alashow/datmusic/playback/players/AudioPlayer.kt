/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.util.PriorityTaskManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import okhttp3.OkHttpClient

interface AudioPlayer {
    fun play(startAtPosition: Long? = null)
    fun setSource(uri: Uri? = null, path: String? = null): Boolean
    fun prepare()
    fun seekTo(position: Long)
    fun duration(): Long
    fun isPrepared(): Boolean
    fun isPlaying(): Boolean
    fun position(): Long
    fun pause()
    fun stop()
    fun release()
    fun onPrepared(prepared: OnPrepared<AudioPlayer>)
    fun onError(error: OnError<AudioPlayer>)
    fun onBuffering(buffering: OnBuffering<AudioPlayer>)
    fun onReady(ready: OnReady<AudioPlayer>)
    fun onCompletion(completion: OnCompletion<AudioPlayer>)
}

class AudioPlayerImpl @Inject constructor(
    @ApplicationContext internal val context: Context,
    @Named("player") private val okHttpClient: OkHttpClient,
) : AudioPlayer, Player.Listener, LoadEventController {

    private var playerBase: ExoPlayer? = null
    private val player: ExoPlayer
        get() {
            if (playerBase == null) {
                playerBase = createPlayer(this)
            }
            return playerBase ?: throw IllegalStateException("Could not create an audio player")
        }

    private var isPrepared = false
    private var isBuffering = true
    private var onPrepared: OnPrepared<AudioPlayer> = {}
    private var onError: OnError<AudioPlayer> = {}
    private var onBuffering: OnBuffering<AudioPlayer> = {}
    private var onReady: OnReady<AudioPlayer> = {}
    private var onCompletion: OnCompletion<AudioPlayer> = {}

    override fun play(startAtPosition: Long?) {
        startAtPosition ?: return player.play()
        player.seekTo(startAtPosition)
        player.play()
    }

    override fun setSource(uri: Uri?, path: String?): Boolean {
        return try {
            uri?.let {
                val mediaSource: MediaSource = ProgressiveMediaSource.Factory(OkHttpDataSource.Factory(okHttpClient))
                    .createMediaSource(MediaItem.fromUri(it))
                player.setMediaSource(mediaSource, true)
            }
            path?.let {
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(it))), true)
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

    override fun isPlaying() = player.isPlaying

    override fun position() = player.currentPosition

    override fun pause() {
        player.pause()
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

    override fun onReady(ready: OnReady<AudioPlayer>) {
        this.onReady = ready
    }

    override fun onCompletion(completion: OnCompletion<AudioPlayer>) {
        this.onCompletion = completion
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            Player.STATE_BUFFERING -> onBuffering(this)
            Player.STATE_READY -> onReady(this)
            Player.STATE_ENDED -> onCompletion(this)
            else -> Unit
        }
    }

    override fun onPrepared() {
        isPrepared = true
        onPrepared(this)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        isPrepared = false
        onError(this, error)
    }

    private fun createPlayer(owner: AudioPlayerImpl): ExoPlayer {
        return SimpleExoPlayer.Builder(context)
            .setLoadControl(
                LoadController().apply {
                    eventController = owner
                }
            )
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
