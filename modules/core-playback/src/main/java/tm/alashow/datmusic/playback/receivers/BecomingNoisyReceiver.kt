/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

class BecomingNoisyReceiver(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {

    private val filter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, filter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}
