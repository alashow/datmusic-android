/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.players

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.core.os.bundleOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.readable
import tm.alashow.datmusic.playback.AudioFocusHelper
import tm.alashow.datmusic.playback.BY_UI_KEY
import tm.alashow.datmusic.playback.PAUSE_ACTION
import tm.alashow.datmusic.playback.PLAY_ACTION
import tm.alashow.datmusic.playback.PLAY_ALL_SHUFFLED
import tm.alashow.datmusic.playback.PLAY_NEXT
import tm.alashow.datmusic.playback.REMOVE_QUEUE_ITEM_BY_ID
import tm.alashow.datmusic.playback.REMOVE_QUEUE_ITEM_BY_POSITION
import tm.alashow.datmusic.playback.REPEAT_ALL
import tm.alashow.datmusic.playback.REPEAT_ONE
import tm.alashow.datmusic.playback.SET_MEDIA_STATE
import tm.alashow.datmusic.playback.SWAP_ACTION
import tm.alashow.datmusic.playback.UPDATE_QUEUE
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.models.toMediaIdList

const val SEEK_TO = "action_seek_to"

const val QUEUE_MEDIA_ID_KEY = "queue_media_id_key"
const val QUEUE_TITLE_KEY = "queue_title_key"
const val QUEUE_LIST_KEY = "queue_list_key"

const val QUEUE_FROM_POSITION_KEY = "queue_from_position_key"
const val QUEUE_TO_POSITION_KEY = "queue_to_position_key"

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val datmusicPlayer: DatmusicPlayer,
    private val audioFocusHelper: AudioFocusHelper,
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

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        Timber.d("onPlayFromMediaId, $mediaId, ${extras?.readable()}")
        launch { datmusicPlayer.setDataFromMediaId(mediaId, extras ?: bundleOf()) }
    }

    override fun onSeekTo(position: Long) {
        Timber.d("onSeekTo: position=$position")
        datmusicPlayer.seekTo(position)
    }

    override fun onSkipToNext() {
        Timber.d("onSkipToNext()")
        launch { datmusicPlayer.nextAudio() }
    }

    override fun onSkipToPrevious() {
        Timber.d("onSkipToPrevious()")
        launch { datmusicPlayer.previousAudio() }
    }

    override fun onSkipToQueueItem(id: Long) {
        Timber.d("onSkipToQueueItem: $id")
        launch { datmusicPlayer.skipTo(id.toInt()) }
    }

    override fun onStop() {
        Timber.d("onStop()")
        datmusicPlayer.stop(byUser = true)
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
        datmusicPlayer.setShuffleMode(shuffleMode)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            SET_MEDIA_STATE -> launch { setSavedMediaSessionState() }
            REPEAT_ONE -> launch { datmusicPlayer.repeatAudio() }
            REPEAT_ALL -> launch { datmusicPlayer.repeatQueue() }
            PAUSE_ACTION -> datmusicPlayer.pause(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_ACTION -> playOnFocus(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_NEXT -> datmusicPlayer.playNext(extras?.getString(QUEUE_MEDIA_ID_KEY) ?: return)
            REMOVE_QUEUE_ITEM_BY_POSITION -> datmusicPlayer.removeFromQueue(extras?.getInt(QUEUE_FROM_POSITION_KEY) ?: return)
            REMOVE_QUEUE_ITEM_BY_ID -> datmusicPlayer.removeFromQueue(extras?.getString(QUEUE_MEDIA_ID_KEY) ?: return)
            UPDATE_QUEUE -> {
                extras ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY)

                datmusicPlayer.updateData(queue, queueTitle)
            }
            PLAY_ALL_SHUFFLED -> {
                extras ?: return

                val controller = mediaSession.controller ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY)
                datmusicPlayer.setData(queue, queueTitle)

                controller.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)

                launch {
                    datmusicPlayer.nextAudio()
                }
            }
            SWAP_ACTION -> {
                extras ?: return
                val from = extras.getInt(QUEUE_FROM_POSITION_KEY)
                val to = extras.getInt(QUEUE_TO_POSITION_KEY)

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
            mediaSession.controller?.queue.toMediaIdList().map { it.value },
            mediaSession.controller?.queueTitle.toString()
        )
    }

    private fun playOnFocus(extras: Bundle = bundleOf(BY_UI_KEY to true)) {
        if (audioFocusHelper.requestPlayback())
            datmusicPlayer.playAudio(extras)
    }
}
