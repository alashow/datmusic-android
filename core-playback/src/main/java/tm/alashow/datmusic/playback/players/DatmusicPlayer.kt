/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.core.os.bundleOf
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.plus
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.AudioFocusHelperImpl
import tm.alashow.datmusic.playback.AudioQueueManagerImpl
import tm.alashow.datmusic.playback.BY_UI_KEY
import tm.alashow.datmusic.playback.QueueState
import tm.alashow.datmusic.playback.R
import tm.alashow.datmusic.playback.REPEAT_ALL
import tm.alashow.datmusic.playback.REPEAT_ONE
import tm.alashow.datmusic.playback.createDefaultPlaybackState
import tm.alashow.datmusic.playback.id
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.position
import tm.alashow.datmusic.playback.repeatMode
import tm.alashow.datmusic.playback.shuffleMode
import tm.alashow.datmusic.playback.toMediaIdList

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit
typealias OnBuffering<T> = T.() -> Unit
typealias OnReady<T> = T.() -> Unit
typealias OnMetaDataChanged = DatmusicPlayer.() -> Unit
typealias OnIsPlaying = DatmusicPlayer.(playing: Boolean, byUi: Boolean) -> Unit

const val REPEAT_MODE = "repeat_mode"
const val SHUFFLE_MODE = "shuffle_mode"

const val DEFAULT_FORWARD_REWIND = 10 * 1000

interface DatmusicPlayer {
    fun getSession(): MediaSessionCompat
    fun playAudio(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun playAudio(id: String)
    fun playAudio(audio: Audio)
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun pause(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun nextAudio(): String?
    suspend fun repeatAudio()
    suspend fun repeatQueue()
    suspend fun previousAudio()
    fun playNext(id: String)
    fun swapQueueAudios(from: Int, to: Int)
    fun removeFromQueue(id: String)
    fun stop(fromUser: Boolean = true)
    fun release()
    fun onPlayingState(playing: OnIsPlaying)
    fun onPrepared(prepared: OnPrepared<DatmusicPlayer>)
    fun onError(error: OnError<DatmusicPlayer>)
    fun onCompletion(completion: OnCompletion<DatmusicPlayer>)
    fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged)
    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit)
    fun setPlaybackState(state: PlaybackStateCompat)
    fun updateData(list: List<String> = emptyList(), title: String = "")
    fun setData(list: List<String> = emptyList(), title: String = "")
    suspend fun saveQueueState()
    suspend fun restoreQueueState()
    fun clearRandomAudioPlayed()
    fun setCurrentAudioId(audioId: String)
    fun shuffleQueue(isShuffle: Boolean)
}

class DatmusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayerImpl,
    private val queueManager: AudioQueueManagerImpl,
    private val audioFocusHelper: AudioFocusHelperImpl,
    private val audiosDao: AudiosDao,
    private val artistDao: ArtistsDao,
    private val albumsDao: AlbumsDao,
    private val preferences: PreferencesStore,
) : DatmusicPlayer, CoroutineScope by MainScope() {

    companion object {
        private const val queueStateKey = "player_queue_state"
    }

    private var isInitialized: Boolean = false

    private var isPlayingCallback: OnIsPlaying = { _, _ -> }
    private var preparedCallback: OnPrepared<DatmusicPlayer> = {}
    private var errorCallback: OnError<DatmusicPlayer> = {}
    private var completionCallback: OnCompletion<DatmusicPlayer> = {}
    private var metaDataChangedCallback: OnMetaDataChanged = {}

    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = createDefaultPlaybackState()

    private val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(Intent.ACTION_MEDIA_BUTTON), FLAG_IMMUTABLE)

    private val mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name), null, pendingIntent).apply {
        setCallback(
            MediaSessionCallback(this, this@DatmusicPlayerImpl, audioFocusHelper, audiosDao, artistDao, albumsDao)
        )
        setPlaybackState(stateBuilder.build())

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, FLAG_IMMUTABLE)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {
        queueManager.setMediaSession(mediaSession)
        audioPlayer.onPrepared {
            preparedCallback(this@DatmusicPlayerImpl)
            launch {
                if (!mediaSession.isPlaying()) audioPlayer.seekTo(mediaSession.position())
                playAudio()
            }
        }

        audioPlayer.onCompletion {
            completionCallback(this@DatmusicPlayerImpl)
            val controller = getSession().controller
            when (controller.repeatMode) {
                REPEAT_MODE_ONE -> controller.transportControls.sendCustomAction(REPEAT_ONE, null)
                REPEAT_MODE_ALL -> controller.transportControls.sendCustomAction(REPEAT_ALL, null)
                else -> launch { if (nextAudio() == null) goToStart() }
            }
        }
        audioPlayer.onBuffering {
            updatePlaybackState {
                setState(STATE_BUFFERING, mediaSession.position(), 1F)
            }
        }
        audioPlayer.onReady {
            if (!audioPlayer.isPlaying()) {
                Timber.d("Player ready but not currently playing, requesting to play")
                audioPlayer.play()
            }
            updatePlaybackState {
                setState(STATE_PLAYING, mediaSession.position(), 1F)
            }
        }
    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun playAudio(extras: Bundle) {
        if (isInitialized) {
            updatePlaybackState {
                setState(STATE_PLAYING, mediaSession.position(), 1F)
                setExtras(
                    extras + bundleOf(
                        REPEAT_MODE to getSession().repeatMode,
                        SHUFFLE_MODE to getSession().shuffleMode
                    )
                )
            }
            audioPlayer.play()
            return
        }

        val isSourceSet = when (val audio = queueManager.currentAudio) {
            is Audio -> audioPlayer.setSource(Uri.parse(audio.streamUrl))
            else -> false
        }

        if (isSourceSet) {
            isInitialized = true
            audioPlayer.prepare()
        } else {
            Timber.e("Couldn't set new source")
        }
    }

    override suspend fun playAudio(id: String) {
        if (audioFocusHelper.requestPlayback()) {
            val audio = audiosDao.entry(id).firstOrNull()
            if (audio != null) {
                playAudio(audio)
            } else {
                Timber.e("Audio by id: $id not found")
                nextAudio()
            }
        }
    }

    override fun playAudio(audio: Audio) {
        queueManager.currentAudioId = audio.id
        queueManager.currentAudio = audio
        isInitialized = false

        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, 0, 1F)
        }
        setMetaData(audio)
        playAudio()
    }

    override fun seekTo(position: Long) {
        if (isInitialized) {
            audioPlayer.seekTo(position)
            updatePlaybackState {
                setState(
                    mediaSession.controller.playbackState.state,
                    position,
                    1F
                )
            }
        } else updatePlaybackState {
            setState(
                mediaSession.controller.playbackState.state,
                position,
                1F
            )
        }
    }

    override fun fastForward() {
        val forwardTo = mediaSession.position() + DEFAULT_FORWARD_REWIND
        queueManager.currentAudio?.apply {
            val duration = durationMillis()
            if (forwardTo > duration) {
                seekTo(duration)
            } else {
                seekTo(forwardTo)
            }
        }
    }

    override fun rewind() {
        val rewindTo = mediaSession.position() - DEFAULT_FORWARD_REWIND
        if (rewindTo < 0) {
            seekTo(0)
        } else {
            seekTo(rewindTo)
        }
    }

    override fun pause(extras: Bundle) {
        if (audioPlayer.isPlaying() && isInitialized) {
            audioPlayer.pause()
            updatePlaybackState {
                setState(STATE_PAUSED, mediaSession.position(), 1F)
                setExtras(
                    extras + bundleOf(
                        REPEAT_MODE to getSession().repeatMode,
                        SHUFFLE_MODE to getSession().shuffleMode
                    )
                )
            }
        } else {
            Timber.d("Couldn't pause player: ${audioPlayer.isPlaying()}, $isInitialized")
        }
    }

    override suspend fun nextAudio(): String? {
        val id = queueManager.nextAudioId
        id?.let { playAudio(it) }
        return id
    }

    override suspend fun repeatAudio() {
        playAudio(queueManager.currentAudioId)
    }

    override suspend fun repeatQueue() {
        if (queueManager.currentAudioId == queueManager.queue.last())
            playAudio(queueManager.queue.first())
        else {
            nextAudio()
        }
    }

    override suspend fun previousAudio() {
        queueManager.previousAudioId?.let {
            playAudio(it)
        } ?: repeatAudio()
    }

    override fun playNext(id: String) {
        queueManager.playNext(id)
    }

    override fun swapQueueAudios(from: Int, to: Int) {
        queueManager.swap(from, to)
        queueManager.currentAudio?.apply { setMetaData(this) }
    }

    override fun removeFromQueue(id: String) {
        queueManager.remove(id)
    }

    override fun stop(byUser: Boolean) {
        audioPlayer.stop()
        updatePlaybackState {
            setState(if (byUser) STATE_NONE else STATE_STOPPED, 0, 1F)
        }
        isInitialized = false
        launch { saveQueueState() }
    }

    override fun release() {
        mediaSession.apply {
            isActive = false
            release()
        }
        audioPlayer.release()
        queueManager.clear()
    }

    override fun onPlayingState(playing: OnIsPlaying) {
        this.isPlayingCallback = playing
    }

    override fun onPrepared(prepared: OnPrepared<DatmusicPlayer>) {
        this.preparedCallback = prepared
    }

    override fun onError(error: OnError<DatmusicPlayer>) {
        this.errorCallback = error
        audioPlayer.onError { throwable ->
            Timber.e(throwable, "AudioPlayer error")
            errorCallback(this@DatmusicPlayerImpl, throwable)
        }
    }

    override fun onCompletion(completion: OnCompletion<DatmusicPlayer>) {
        this.completionCallback = completion
    }

    override fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged) {
        this.metaDataChangedCallback = metaDataChanged
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
        isPlayingCallback(state.isPlaying, state.extras?.getBoolean(BY_UI_KEY) ?: false)
    }

    override fun updateData(list: List<String>, title: String) {
        if (mediaSession.shuffleMode == SHUFFLE_MODE_NONE)
            if (title == queueManager.queueTitle) {
                queueManager.queue = list
                queueManager.queueTitle = title
                queueManager.currentAudio?.apply { setMetaData(this) }
            }
    }

    override fun setData(list: List<String>, title: String) {
        queueManager.queue = list
        queueManager.queueTitle = title
    }

    override suspend fun saveQueueState() {
        val mediaSession = getSession()
        val controller = mediaSession.controller
        if (controller == null || controller.playbackState == null) {
            Timber.d("Not saving queue state")
            return
        }

        val id = controller.metadata?.id ?: return

        val queueState = QueueState(
            queue = mediaSession.controller.queue.toMediaIdList(),
            currentId = id,
            seekPosition = controller.playbackState?.position ?: 0,
            repeatMode = controller.repeatMode,
            shuffleMode = controller.shuffleMode,
            state = controller.playbackState?.state ?: PlaybackState.STATE_NONE,
            name = controller.queueTitle?.toString() ?: "All"
        )

        Timber.d("Saving queue state: ${queueState.currentId}, size=${queueState.queue.size}")
        preferences.save(queueStateKey, queueState, QueueState.serializer())
    }

    override suspend fun restoreQueueState() {
        Timber.d("Restoring queue state")
        var queueState = preferences.get(queueStateKey, QueueState.serializer(), QueueState(emptyList())).first()
        Timber.d("Saved state: ${queueState.currentId}, size=${queueState.queue.size}")
        if (queueState.state in listOf(STATE_PLAYING, STATE_BUFFERING, STATE_BUFFERING)) {
            queueState = queueState.copy(state = STATE_PAUSED)
        }

        queueManager.currentAudioId = queueState.currentId

        setData(queueState.queue, queueState.name)

        queueManager.currentAudio()?.apply {
            Timber.d("Setting metadata from saved state: $id")
            setMetaData(this)
        }

        val extras = bundleOf(
            REPEAT_MODE to queueState.repeatMode,
            SHUFFLE_MODE to queueState.shuffleMode
        )

        updatePlaybackState {
            setState(queueState.state, queueState.seekPosition, 1F)
            setExtras(extras)
        }
    }

    override fun clearRandomAudioPlayed() {
        queueManager.clearPlayedAudios()
    }

    override fun setCurrentAudioId(audioId: String) {
        queueManager.currentAudioId = audioId
    }

    override fun shuffleQueue(isShuffle: Boolean) {
        queueManager.shuffleQueue(isShuffle)
        queueManager.currentAudio?.apply { setMetaData(this) }
    }

    private fun goToStart() {
        isInitialized = false

        stop()

        if (queueManager.queue.isEmpty()) return

        launch {
            queueManager.currentAudioId = queueManager.queue.first()
            queueManager.currentAudio()?.apply { setMetaData(this) }
        }
    }

    private fun setMetaData(audio: Audio) {
        val player = this
        launch {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(audio.coverUri())
                .allowHardware(false)
                .build()

            val bitmap = when (val result = loader.execute(request)) {
                is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
                else -> null
            }

            val mediaMetadata = metadataBuilder.apply {
                putString(METADATA_KEY_ALBUM, audio.album)
                putString(METADATA_KEY_ARTIST, audio.artist)
                putString(METADATA_KEY_TITLE, audio.title)
                putString(METADATA_KEY_ALBUM_ART_URI, audio.coverUri().toString())
                putString(METADATA_KEY_MEDIA_ID, audio.id)
                putLong(METADATA_KEY_DURATION, audio.durationMillis())
                putBitmap(METADATA_KEY_ALBUM_ART, bitmap)
            }.build()
            mediaSession.setMetadata(mediaMetadata)
            metaDataChangedCallback(player)
        }
    }
}
