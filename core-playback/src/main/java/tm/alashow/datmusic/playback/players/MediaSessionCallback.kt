/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import androidx.core.os.bundleOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.orNA
import tm.alashow.base.util.extensions.readable
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.playback.AudioFocusHelper
import tm.alashow.datmusic.playback.BY_UI_KEY
import tm.alashow.datmusic.playback.PAUSE_ACTION
import tm.alashow.datmusic.playback.PLAY_ACTION
import tm.alashow.datmusic.playback.PLAY_ALL_SHUFFLED
import tm.alashow.datmusic.playback.REPEAT_ALL
import tm.alashow.datmusic.playback.REPEAT_ONE
import tm.alashow.datmusic.playback.SET_MEDIA_STATE
import tm.alashow.datmusic.playback.SWAP_ACTION
import tm.alashow.datmusic.playback.UPDATE_QUEUE
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.toAudioList
import tm.alashow.datmusic.playback.toMediaId
import tm.alashow.datmusic.playback.toMediaIdList
import tm.alashow.datmusic.playback.toQueueTitle

const val SEEK_TO_POS = "seek_to_pos"
const val SEEK_TO = "action_seek_to"
const val SONG_LIST_NAME = "song_list_name"

const val FROM_POSITION_KEY = "from_position_key"
const val TO_POSITION_KEY = "to_position_key"

const val QUEUE_TITLE_KEY = "queue_info_key"
const val QUEUE_LIST_KEY = "queue_list_key"

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val datmusicPlayer: DatmusicPlayer,
    private val audioFocusHelper: AudioFocusHelper,
    private val audiosDao: AudiosDao,
    private val artistsDao: ArtistsDao,
    private val albumsDao: AlbumsDao,
) : MediaSessionCompat.Callback(), CoroutineScope by MainScope() {

    init {
        audioFocusHelper.onAudioFocusGain {
            Timber.d("GAIN")
            if (isAudioFocusGranted && !datmusicPlayer.getSession().isPlaying()) {
                datmusicPlayer.playAudio()
            } else audioFocusHelper.setVolume(AudioManager.ADJUST_RAISE)
            isAudioFocusGranted = false
        }
        audioFocusHelper.onAudioFocusLoss {
            Timber.d("LOSS")
            abandonPlayback()
            isAudioFocusGranted = false
            datmusicPlayer.pause()
        }

        audioFocusHelper.onAudioFocusLossTransient {
            Timber.d("TRANSIENT")
            if (datmusicPlayer.getSession().isPlaying()) {
                isAudioFocusGranted = true
                datmusicPlayer.pause()
            }
        }

        audioFocusHelper.onAudioFocusLossTransientCanDuck {
            Timber.d("TRANSIENT_CAN_DUCK")
            audioFocusHelper.setVolume(AudioManager.ADJUST_LOWER)
        }
    }

    override fun onPause() {
        Timber.d("onPause")
        datmusicPlayer.pause()
    }

    override fun onPlay() {
        Timber.d("onPlay")
        playOnFocus()
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        Timber.d("onPlayFromSearch, query = $query, ${extras?.readable()}")
        query?.let {
            // val audio = findAudioForQuery(query)
            // if (audio != null) {
            //     launch {
            //         musicPlayer.playAudio(audio)
            //     }
            // }
        } ?: onPlay()
    }

    override fun onFastForward() {
        Timber.d("onFastForward")
        datmusicPlayer.fastForward()
    }

    override fun onRewind() {
        Timber.d("onRewind")
        datmusicPlayer.rewind()
    }

    override fun onPlayFromMediaId(_mediaId: String, extras: Bundle?) {
        val mediaId = _mediaId.toMediaId()
        Timber.d("onPlayFromMediaId, $mediaId, ${extras?.readable()}")
        launch {
            var audioId = mediaId.id
            var queue = extras?.getStringArray(QUEUE_LIST_KEY)?.toList()
            var queueTitle = extras?.getString(QUEUE_TITLE_KEY)
            val seekTo = extras?.getLong(SEEK_TO) ?: 0

            if (seekTo > 0) datmusicPlayer.seekTo(seekTo)

            if (queue == null) {
                queue = mediaId.toAudioList(audiosDao, artistsDao, albumsDao)?.map { it.id }?.apply {
                    if (isNotEmpty())
                        audioId = if (mediaId.index < size) get(mediaId.index) else first()
                }
                queueTitle = mediaId.toQueueTitle(audiosDao, artistsDao, albumsDao)
            }

            if (queue != null && queue.isNotEmpty()) {
                datmusicPlayer.setCurrentAudioId(audioId)
                datmusicPlayer.setData(queue, queueTitle.orNA())
                launch {
                    // delay for new queue to apply first
                    delay(2500)
                    datmusicPlayer.saveQueueState()
                }
                datmusicPlayer.playAudio(audioId)
            } else {
                Timber.e("Queue is null or empty: $mediaId")
            }
        }
    }

    override fun onSeekTo(pos: Long) {
        Timber.d("onSeekTo()")
        datmusicPlayer.seekTo(pos)
    }

    override fun onSkipToNext() {
        Timber.d("onSkipToNext()")
        launch { datmusicPlayer.nextAudio() }
    }

    override fun onSkipToPrevious() {
        Timber.d("onSkipToPrevious()")
        launch { datmusicPlayer.previousAudio() }
    }

    override fun onStop() {
        Timber.d("onStop()")
        datmusicPlayer.stop()
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        datmusicPlayer.setPlaybackState(
            Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(REPEAT_MODE, repeatMode)
                    }
                ).build()
        )
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        datmusicPlayer.setPlaybackState(
            Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(SHUFFLE_MODE, shuffleMode)
                    }
                ).build()
        )
        datmusicPlayer.shuffleQueue(shuffleMode != SHUFFLE_MODE_NONE)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            SET_MEDIA_STATE -> launch { setSavedMediaSessionState() }
            REPEAT_ONE -> launch { datmusicPlayer.repeatAudio() }
            REPEAT_ALL -> launch { datmusicPlayer.repeatQueue() }
            PAUSE_ACTION -> datmusicPlayer.pause(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_ACTION -> playOnFocus(extras ?: bundleOf(BY_UI_KEY to true))
            UPDATE_QUEUE -> {
                extras ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY).orNA()

                datmusicPlayer.updateData(queue, queueTitle)
            }
            PLAY_ALL_SHUFFLED -> {
                extras ?: return

                val controller = mediaSession.controller ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY).orNA()
                datmusicPlayer.setData(queue, queueTitle)

                controller.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)

                launch {
                    datmusicPlayer.nextAudio()
                }
            }
            SWAP_ACTION -> {
                extras ?: return
                val from = extras.getInt(FROM_POSITION_KEY)
                val to = extras.getInt(TO_POSITION_KEY)

                datmusicPlayer.swapQueueAudios(from, to)
            }
        }
    }

    private suspend fun setSavedMediaSessionState() {
        val controller = mediaSession.controller ?: return
        Timber.d(controller.playbackState.toString())
        if (controller.playbackState == null || controller.playbackState.state == STATE_NONE) {
            datmusicPlayer.restoreQueueState()
        } else {
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        mediaSession.setMetadata(mediaSession.controller.metadata)
        datmusicPlayer.setPlaybackState(mediaSession.controller.playbackState)
        datmusicPlayer.setData(
            mediaSession.controller.queue.toMediaIdList(),
            mediaSession.controller.queueTitle.toString()
        )
    }

    private fun playOnFocus(extras: Bundle = bundleOf(BY_UI_KEY to true)) {
        if (audioFocusHelper.requestPlayback())
            datmusicPlayer.playAudio(extras)
    }
}
