/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.imageloading.getBitmap
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.plus
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.artworkFromFile
import tm.alashow.datmusic.playback.AudioFocusHelperImpl
import tm.alashow.datmusic.playback.AudioQueueManagerImpl
import tm.alashow.datmusic.playback.BY_UI_KEY
import tm.alashow.datmusic.playback.MediaQueueBuilder
import tm.alashow.datmusic.playback.R
import tm.alashow.datmusic.playback.REPEAT_ALL
import tm.alashow.datmusic.playback.REPEAT_ONE
import tm.alashow.datmusic.playback.createDefaultPlaybackState
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO
import tm.alashow.datmusic.playback.models.MediaId
import tm.alashow.datmusic.playback.models.QueueState
import tm.alashow.datmusic.playback.models.toMediaId
import tm.alashow.datmusic.playback.models.toMediaMetadata
import tm.alashow.datmusic.playback.position
import tm.alashow.datmusic.playback.repeatMode
import tm.alashow.datmusic.playback.shuffleMode

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit
typealias OnBuffering<T> = T.() -> Unit
typealias OnReady<T> = T.() -> Unit
typealias OnMetaDataChanged = DatmusicPlayer.() -> Unit
typealias OnIsPlaying<T> = T.(playing: Boolean, byUi: Boolean) -> Unit

const val REPEAT_MODE = "repeat_mode"
const val SHUFFLE_MODE = "shuffle_mode"
const val QUEUE_CURRENT_INDEX = "queue_current_index"
const val QUEUE_HAS_PREVIOUS = "queue_has_previous"
const val QUEUE_HAS_NEXT = "queue_has_next"

const val DEFAULT_FORWARD_REWIND = 10 * 1000

interface DatmusicPlayer {
    fun getSession(): MediaSessionCompat
    fun playAudio(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun playAudio(id: String, index: Int? = null)
    suspend fun playAudio(audio: Audio, index: Int? = null)
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun pause(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun nextAudio(): String?
    suspend fun repeatAudio()
    suspend fun repeatQueue()
    suspend fun previousAudio()
    fun playNext(id: String)
    suspend fun skipTo(position: Int)
    fun removeFromQueue(position: Int)
    fun removeFromQueue(id: String)
    fun swapQueueAudios(from: Int, to: Int)
    fun stop(byUser: Boolean)
    fun release()
    fun onPlayingState(playing: OnIsPlaying<DatmusicPlayer>)
    fun onPrepared(prepared: OnPrepared<DatmusicPlayer>)
    fun onError(error: OnError<DatmusicPlayer>)
    fun onCompletion(completion: OnCompletion<DatmusicPlayer>)
    fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged)
    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit = {})
    fun setPlaybackState(state: PlaybackStateCompat)
    fun setShuffleMode(shuffleMode: Int)
    fun updateData(list: List<String> = emptyList(), title: String? = null)
    fun setData(list: List<String> = emptyList(), title: String? = null)
    suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle = bundleOf())
    suspend fun saveQueueState()
    suspend fun restoreQueueState()
    fun clearRandomAudioPlayed()
    fun setCurrentAudioId(audioId: String, index: Int? = null)
    fun shuffleQueue(isShuffle: Boolean)
}

@Singleton
class DatmusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayerImpl,
    private val queueManager: AudioQueueManagerImpl,
    private val audioFocusHelper: AudioFocusHelperImpl,
    private val audiosRepo: AudiosRepo,
    private val mediaQueueBuilder: MediaQueueBuilder,
    private val preferences: PreferencesStore,
    private val analytics: Analytics,
) : DatmusicPlayer, CoroutineScope by MainScope() {

    companion object {
        private const val queueStateKey = "player_queue_state"
    }

    private var isInitialized: Boolean = false

    private var isPlayingCallback: OnIsPlaying<DatmusicPlayer> = { _, _ -> }
    private var preparedCallback: OnPrepared<DatmusicPlayer> = {}
    private var errorCallback: OnError<DatmusicPlayer> = {}
    private var completionCallback: OnCompletion<DatmusicPlayer> = {}
    private var metaDataChangedCallback: OnMetaDataChanged = {}

    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = createDefaultPlaybackState()

    private val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(Intent.ACTION_MEDIA_BUTTON), FLAG_IMMUTABLE)

    private val mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name), null, pendingIntent).apply {
        setCallback(
            MediaSessionCallback(this, this@DatmusicPlayerImpl, audioFocusHelper)
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
        audioPlayer.onIsPlaying { playing, byUi ->
            if (playing)
                updatePlaybackState {
                    setState(STATE_PLAYING, mediaSession.position(), 1F)
                    setExtras(
                        bundleOf(
                            REPEAT_MODE to getSession().repeatMode,
                            SHUFFLE_MODE to getSession().shuffleMode
                        )
                    )
                }
            isPlayingCallback(playing, byUi)
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
        audioPlayer.onError { throwable ->
            Timber.e(throwable, "AudioPlayer error")
            errorCallback(this@DatmusicPlayerImpl, throwable)
            isInitialized = false
            updatePlaybackState {
                setState(STATE_ERROR, 0, 1F)
            }
        }
    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun pause(extras: Bundle) {
        if (isInitialized && (audioPlayer.isPlaying() || audioPlayer.isBuffering())) {
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

    override fun playAudio(extras: Bundle) {
        if (isInitialized) {
            audioPlayer.play()
            return
        }

        val isSourceSet = when (val audio = queueManager.currentAudio) {
            is Audio -> {
                when (val downloadItem = audio.audioDownloadItem) {
                    is AudioDownloadItem -> audioPlayer.setSource(downloadItem.downloadInfo.fileUri, true)
                    else -> {
                        val uri = audio.streamUrl?.toUri()
                        if (uri != null)
                            audioPlayer.setSource(uri, false)
                        else false
                    }
                }
            }
            else -> false
        }

        if (isSourceSet) {
            isInitialized = true
            audioPlayer.prepare()
        } else {
            Timber.e("Couldn't set new source")
        }
    }

    override suspend fun playAudio(id: String, index: Int?) {
        if (audioFocusHelper.requestPlayback()) {
            val audio = audiosRepo.find(id)
            if (audio != null) playAudio(audio, index)
            else {
                Timber.e("Audio by id: $id not found")
                updatePlaybackState {
                    setState(STATE_ERROR, 0, 1F)
                }
            }
        }
    }

    override suspend fun playAudio(audio: Audio, index: Int?) {
        setCurrentAudioId(audio.id, index)
        val refreshedAudio = queueManager.refreshCurrentAudio()
        isInitialized = false

        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, 0, 1F)
        }
        setMetaData(refreshedAudio ?: audio)
        playAudio()
    }

    override suspend fun skipTo(position: Int) {
        if (queueManager.currentAudioIndex == position) {
            Timber.d("Not skipping to index=$position")
            return
        }
        queueManager.skipTo(position)
        playAudio(queueManager.currentAudioId, position)
        updatePlaybackState()
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
        logEvent("seekTo")
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
        logEvent("fastForward")
    }

    override fun rewind() {
        val rewindTo = mediaSession.position() - DEFAULT_FORWARD_REWIND
        if (rewindTo < 0) {
            seekTo(0)
        } else {
            seekTo(rewindTo)
        }
        logEvent("rewind")
    }

    override suspend fun nextAudio(): String? {
        val index = queueManager.nextAudioIndex
        if (index != null) {
            val id = queueManager.queue[index]
            playAudio(id, index)
            logEvent("nextAudio")
            return id
        }
        return null
    }

    override suspend fun previousAudio() {
        if (queueManager.queue.isNotEmpty())
            queueManager.previousAudioIndex?.let {
                playAudio(queueManager.queue[it], it)
                logEvent("previousAudio")
            } ?: repeatAudio()
    }

    override suspend fun repeatAudio() {
        playAudio(queueManager.currentAudioId)
        logEvent("repeatAudio")
    }

    override suspend fun repeatQueue() {
        if (queueManager.queue.isNotEmpty()) {
            if (queueManager.currentAudioId == queueManager.queue.last())
                playAudio(queueManager.queue.first())
            else {
                nextAudio()
            }
            logEvent("repeatQueue")
        }
    }

    override fun playNext(id: String) {
        if (queueManager.queue.isEmpty()) {
            launch {
                setDataFromMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
            }
        } else {
            queueManager.playNext(id)
        }
        logEvent("playNext", id)
    }

    override fun removeFromQueue(position: Int) {
        queueManager.remove(position)
        logEvent("removeFromQueue", "position=$position")
    }

    override fun removeFromQueue(id: String) {
        queueManager.remove(id)
        logEvent("removeFromQueue", id)
    }

    override fun swapQueueAudios(from: Int, to: Int) {
        queueManager.swap(from, to)
        queueManager.currentAudio?.apply { setMetaData(this) }
        logEvent("nextAudio")
    }

    override fun stop(byUser: Boolean) {
        updatePlaybackState {
            setState(if (byUser) STATE_NONE else STATE_STOPPED, 0, 1F)
        }
        isInitialized = false
        audioPlayer.stop()
        isPlayingCallback(false, byUser)
        queueManager.clear()
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

    override fun onPlayingState(playing: OnIsPlaying<DatmusicPlayer>) {
        this.isPlayingCallback = playing
    }

    override fun onPrepared(prepared: OnPrepared<DatmusicPlayer>) {
        this.preparedCallback = prepared
    }

    override fun onError(error: OnError<DatmusicPlayer>) {
        this.errorCallback = error
    }

    override fun onCompletion(completion: OnCompletion<DatmusicPlayer>) {
        this.completionCallback = completion
    }

    override fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged) {
        this.metaDataChangedCallback = metaDataChanged
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        stateBuilder.setExtras(
            stateBuilder.build().extras + bundleOf(
                QUEUE_CURRENT_INDEX to queueManager.currentAudioIndex,
                QUEUE_HAS_PREVIOUS to (queueManager.previousAudioIndex != null),
                QUEUE_HAS_NEXT to (queueManager.nextAudioIndex != null),
            )
        )
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
    }

    override fun setShuffleMode(shuffleMode: Int) {
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        setPlaybackState(
            PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(SHUFFLE_MODE, shuffleMode)
                    }
                ).build()
        )
        shuffleQueue(shuffleMode != SHUFFLE_MODE_NONE)
    }

    override fun updateData(list: List<String>, title: String?) {
        if (mediaSession.shuffleMode == SHUFFLE_MODE_NONE)
            if (title == queueManager.queueTitle) {
                queueManager.queue = list
                queueManager.queueTitle = title
                queueManager.currentAudio?.apply { setMetaData(this) }
            }
    }

    override fun setData(list: List<String>, title: String?) {
        // reset shuffle mode on new data
        getSession().setShuffleMode(SHUFFLE_MODE_NONE)
        updatePlaybackState {
            setExtras(bundleOf(SHUFFLE_MODE to SHUFFLE_MODE_NONE))
        }

        queueManager.queue = list
        queueManager.queueTitle = title ?: ""
    }

    override suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle) {
        val mediaId = _mediaId.toMediaId()
        var audioId = extras.getString(QUEUE_MEDIA_ID_KEY) ?: mediaId.value
        var queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList()
        var queueTitle = extras.getString(QUEUE_TITLE_KEY)
        val seekTo = extras.getLong(SEEK_TO)

        if (seekTo > 0) seekTo(seekTo)

        if (queue == null)
            queue = mediaQueueBuilder.buildAudioList(mediaId).map { it.id }

        if (queueTitle.isNullOrBlank())
            queueTitle = mediaQueueBuilder.buildQueueTitle(mediaId).toString()

        if (queue.isNotEmpty()) {
            with(queue) {
                when {
                    mediaId.isShuffleIndex -> audioId = shuffled().first()
                    mediaId.hasIndex -> audioId = if (mediaId.index < size) get(mediaId.index) else first()
                }
            }

            setData(queue, queueTitle)
            playAudio(audioId, if (mediaId.hasIndex) mediaId.index else queue.indexOf(audioId))
            if (mediaId.isShuffleIndex) setShuffleMode(SHUFFLE_MODE_ALL)
            // delay for new queue to apply first
            delay(2000)
            saveQueueState()
        } else {
            Timber.e("Queue is null or empty: $mediaId")
        }

        logEvent("playMedia", _mediaId)
    }

    override suspend fun saveQueueState() {
        val mediaSession = getSession()
        val controller = mediaSession.controller
        if (controller == null || controller.playbackState == null) {
            Timber.d("Not saving queue state")
            return
        }

        val queueState = QueueState(
            queue = queueManager.queue,
            currentIndex = queueManager.currentAudioIndex,
            seekPosition = controller.playbackState?.position ?: 0,
            repeatMode = controller.repeatMode,
            shuffleMode = controller.shuffleMode,
            state = controller.playbackState?.state ?: PlaybackState.STATE_NONE,
            title = controller.queueTitle?.toString()
        )

        Timber.d("Saving queue state: idx=${queueState.currentIndex}, size=${queueState.queue.size}, title=${queueState.title}")
        preferences.save(queueStateKey, queueState, QueueState.serializer())
    }

    override suspend fun restoreQueueState() {
        Timber.d("Restoring queue state")
        var queueState = preferences.get(queueStateKey, QueueState.serializer(), QueueState(emptyList())).first()
        Timber.d("Restoring state: ${queueState.currentIndex}, size=${queueState.queue.size}")

        if (queueState.state in listOf(STATE_PLAYING, STATE_BUFFERING, STATE_BUFFERING, STATE_ERROR)) {
            queueState = queueState.copy(state = STATE_PAUSED)
        }

        if (queueState.queue.isNotEmpty()) {
            setCurrentAudioId(queueState.queue[queueState.currentIndex], queueState.currentIndex)

            setData(queueState.queue, queueState.title ?: "")

            queueManager.refreshCurrentAudio()?.apply {
                Timber.d("Setting metadata from saved state: currentAudio=$id")
                setMetaData(this)
            }
        }

        val extras = bundleOf(
            REPEAT_MODE to queueState.repeatMode,
            SHUFFLE_MODE to SHUFFLE_MODE_NONE
        )

        updatePlaybackState {
            setState(queueState.state, queueState.seekPosition, 1F)
            setExtras(extras)
        }
    }

    override fun clearRandomAudioPlayed() {
        queueManager.clearPlayedAudios()
    }

    override fun setCurrentAudioId(audioId: String, index: Int?) {
        val audioIndex = index ?: queueManager.queue.indexOfFirst { it == audioId }
        if (audioIndex < 0) {
            error("Audio id isn't in the queue, what it do?")
        } else queueManager.currentAudioIndex = audioIndex
    }

    override fun shuffleQueue(isShuffle: Boolean) {
        launch {
            queueManager.shuffleQueue(isShuffle)
            queueManager.currentAudio?.apply { setMetaData(this) }
            updatePlaybackState {
                setState(mediaSession.controller.playbackState.state, mediaSession.position(), 1F)
            }
            logEvent("shuffleQueue")
        }
    }

    private fun goToStart() {
        isInitialized = false

        stop(byUser = false)

        if (queueManager.queue.isEmpty()) return

        launch {
            setCurrentAudioId(queueManager.queue.first())
            queueManager.refreshCurrentAudio()?.apply { setMetaData(this) }
        }
    }

    private fun setMetaData(audio: Audio) {
        val player = this
        launch {
            val mediaMetadata = audio.toMediaMetadata(metadataBuilder).apply {
                val artworkFromFile = audio.artworkFromFile(context)
                if (artworkFromFile != null) {
                    putBitmap(METADATA_KEY_ALBUM_ART, artworkFromFile)
                }
            }
            mediaSession.setMetadata(mediaMetadata.build())
            metaDataChangedCallback(player)

            // cover image is applied separately to avoid delaying metadata setting while fetching bitmap from network
            val smallCoverBitmap = context.getBitmap(audio.coverUri(CoverImageSize.SMALL), CoverImageSize.SMALL.maxSize)
            val updatedMetadata = mediaMetadata.apply { putBitmap(METADATA_KEY_ALBUM_ART, smallCoverBitmap) }.build()
            mediaSession.setMetadata(updatedMetadata)
            metaDataChangedCallback(player)
        }
    }

    private fun logEvent(event: String, mediaId: String = queueManager.currentAudioId) = analytics.event("player.$event", mapOf("mediaId" to mediaId))
}
