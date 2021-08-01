/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
import android.media.AudioManager.FLAG_PLAY_SOUND
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.AudioManager.STREAM_MUSIC
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import tm.alashow.base.ui.utils.extensions.systemService
import tm.alashow.base.util.extensions.isOreo

typealias OnAudioFocusGain = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLoss = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLossTransient = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLossTransientCanDuck = AudioFocusHelper.() -> Unit

interface AudioFocusHelper {
    var isAudioFocusGranted: Boolean

    fun requestPlayback(): Boolean
    fun abandonPlayback()
    fun onAudioFocusGain(audioFocusGain: OnAudioFocusGain)
    fun onAudioFocusLoss(audioFocusLoss: OnAudioFocusLoss)
    fun onAudioFocusLossTransient(audioFocusLossTransient: OnAudioFocusLossTransient)
    fun onAudioFocusLossTransientCanDuck(audioFocusLossTransientCanDuck: OnAudioFocusLossTransientCanDuck)
    fun setVolume(volume: Int)
}

class AudioFocusHelperImpl @Inject constructor(
    @ApplicationContext context: Context
) : AudioFocusHelper, OnAudioFocusChangeListener {

    private val audioManager: AudioManager = context.systemService(AUDIO_SERVICE)

    private var audioFocusGainCallback: OnAudioFocusGain = {}
    private var audioFocusLossCallback: OnAudioFocusLoss = {}
    private var audioFocusLossTransientCallback: OnAudioFocusLossTransient = {}
    private var audioFocusLossTransientCanDuckCallback: OnAudioFocusLossTransientCanDuck = {}

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> audioFocusGainCallback()
            AUDIOFOCUS_LOSS -> audioFocusLossCallback()
            AUDIOFOCUS_LOSS_TRANSIENT -> audioFocusLossTransientCallback()
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> audioFocusLossTransientCanDuckCallback()
        }
    }

    override var isAudioFocusGranted: Boolean = false

    override fun requestPlayback(): Boolean {
        val state = if (isOreo()) {
            val attr = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
            }.build()
            audioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attr)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            )
        } else audioManager.requestAudioFocus(this, STREAM_MUSIC, AUDIOFOCUS_GAIN)
        return state == AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun abandonPlayback() {
        if (isOreo()) {
            val attr = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
            }.build()
            audioManager.abandonAudioFocusRequest(
                AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(attr)
                    .build()
            )
        } else audioManager.abandonAudioFocus(this)
    }

    override fun onAudioFocusGain(audioFocusGain: OnAudioFocusGain) {
        audioFocusGainCallback = audioFocusGain
    }

    override fun onAudioFocusLoss(audioFocusLoss: OnAudioFocusLoss) {
        audioFocusLossCallback = audioFocusLoss
    }

    override fun onAudioFocusLossTransient(audioFocusLossTransient: OnAudioFocusLossTransient) {
        audioFocusLossTransientCallback = audioFocusLossTransient
    }

    override fun onAudioFocusLossTransientCanDuck(audioFocusLossTransientCanDuck: OnAudioFocusLossTransientCanDuck) {
        audioFocusLossTransientCanDuckCallback = audioFocusLossTransientCanDuck
    }

    override fun setVolume(volume: Int) {
        audioManager.adjustVolume(volume, FLAG_PLAY_SOUND)
    }
}
