/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.BroadcastReceiver.PendingResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import java.lang.IllegalStateException

/**
 * Copy of androidx.media.session.MediaButtonReceiver to set FLAG_MUTABLE to pending intents.
 * Converted to Kotlin via Intellij
 */
class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent == null || Intent.ACTION_MEDIA_BUTTON != intent.action ||
            !intent.hasExtra(Intent.EXTRA_KEY_EVENT)
        ) {
            Log.d(TAG, "Ignore unsupported intent: $intent")
            return
        }
        val mediaButtonServiceComponentName = getServiceComponentByAction(context, Intent.ACTION_MEDIA_BUTTON)
        if (mediaButtonServiceComponentName != null) {
            intent.component = mediaButtonServiceComponentName
            ContextCompat.startForegroundService(context, intent)
            return
        }
        val mediaBrowserServiceComponentName = getServiceComponentByAction(
            context,
            MediaBrowserServiceCompat.SERVICE_INTERFACE
        )
        if (mediaBrowserServiceComponentName != null) {
            val pendingResult = goAsync()
            val applicationContext = context.applicationContext
            val connectionCallback = MediaButtonConnectionCallback(applicationContext, intent, pendingResult)
            val mediaBrowser = MediaBrowserCompat(
                applicationContext,
                mediaBrowserServiceComponentName, connectionCallback, null
            )
            connectionCallback.setMediaBrowser(mediaBrowser)
            mediaBrowser.connect()
            return
        }
        throw IllegalStateException(
            "Could not find any Service that handles " +
                Intent.ACTION_MEDIA_BUTTON + " or implements a media browser service."
        )
    }

    private class MediaButtonConnectionCallback internal constructor(
        private val mContext: Context,
        private val mIntent: Intent,
        private val mPendingResult: PendingResult
    ) : MediaBrowserCompat.ConnectionCallback() {
        private var mMediaBrowser: MediaBrowserCompat? = null
        fun setMediaBrowser(mediaBrowser: MediaBrowserCompat?) {
            mMediaBrowser = mediaBrowser
        }

        override fun onConnected() {
            val mediaController = MediaControllerCompat(
                mContext,
                mMediaBrowser!!.sessionToken
            )
            val ke = mIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            mediaController.dispatchMediaButtonEvent(ke)
            finish()
        }

        override fun onConnectionSuspended() {
            finish()
        }

        override fun onConnectionFailed() {
            finish()
        }

        private fun finish() {
            mMediaBrowser!!.disconnect()
            mPendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "MediaButtonReceiver"

        /**
         * Extracts any available [KeyEvent] from an [Intent.ACTION_MEDIA_BUTTON]
         * intent, passing it onto the [MediaSessionCompat] using
         * [MediaControllerCompat.dispatchMediaButtonEvent], which in turn
         * will trigger callbacks to the [MediaSessionCompat.Callback] registered via
         * [MediaSessionCompat.setCallback].
         *
         * @param mediaSessionCompat A [MediaSessionCompat] that has a
         * [MediaSessionCompat.Callback] set.
         * @param intent             The intent to parse.
         * @return The extracted [KeyEvent] if found, or null.
         */
        fun handleIntent(mediaSessionCompat: MediaSessionCompat?, intent: Intent?): KeyEvent? {
            if (mediaSessionCompat == null || intent == null || Intent.ACTION_MEDIA_BUTTON != intent.action ||
                !intent.hasExtra(Intent.EXTRA_KEY_EVENT)
            ) {
                return null
            }
            val ke = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            val mediaController = mediaSessionCompat.controller
            mediaController.dispatchMediaButtonEvent(ke)
            return ke
        }

        /**
         * Creates a broadcast pending intent that will send a media button event. The `action`
         * will be translated to the appropriate [KeyEvent], and it will be sent to the
         * registered media button receiver in the given context. The `action` should be one of
         * the following:
         *
         *  * [PlaybackStateCompat.ACTION_PLAY]
         *  * [PlaybackStateCompat.ACTION_PAUSE]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_NEXT]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS]
         *  * [PlaybackStateCompat.ACTION_STOP]
         *  * [PlaybackStateCompat.ACTION_FAST_FORWARD]
         *  * [PlaybackStateCompat.ACTION_REWIND]
         *  * [PlaybackStateCompat.ACTION_PLAY_PAUSE]
         *
         *
         * @param context The context of the application.
         * @param action  The action to be sent via the pending intent.
         * @return Created pending intent, or null if cannot find a unique registered media button
         * receiver or if the `action` is unsupported/invalid.
         */
        fun buildMediaButtonPendingIntent(
            context: Context,
            @MediaKeyAction action: Long
        ): PendingIntent? {
            val mbrComponent = getMediaButtonReceiverComponent(context)
            if (mbrComponent == null) {
                Log.w(
                    TAG,
                    "A unique media button receiver could not be found in the given context, so " +
                        "couldn't build a pending intent."
                )
                return null
            }
            return buildMediaButtonPendingIntent(context, mbrComponent, action)
        }

        /**
         * Creates a broadcast pending intent that will send a media button event. The `action`
         * will be translated to the appropriate [KeyEvent], and sent to the provided media
         * button receiver via the pending intent. The `action` should be one of the following:
         *
         *  * [PlaybackStateCompat.ACTION_PLAY]
         *  * [PlaybackStateCompat.ACTION_PAUSE]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_NEXT]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS]
         *  * [PlaybackStateCompat.ACTION_STOP]
         *  * [PlaybackStateCompat.ACTION_FAST_FORWARD]
         *  * [PlaybackStateCompat.ACTION_REWIND]
         *  * [PlaybackStateCompat.ACTION_PLAY_PAUSE]
         *
         *
         * @param context      The context of the application.
         * @param mbrComponent The full component name of a media button receiver where you want to send
         * this intent.
         * @param action       The action to be sent via the pending intent.
         * @return Created pending intent, or null if the given component name is null or the
         * `action` is unsupported/invalid.
         */
        @SuppressLint("WrongConstant") // PENDING_INTENT_FLAG_MUTABLE
        fun buildMediaButtonPendingIntent(
            context: Context?,
            mbrComponent: ComponentName?,
            @MediaKeyAction action: Long
        ): PendingIntent? {
            if (mbrComponent == null) {
                Log.w(TAG, "The component name of media button receiver should be provided.")
                return null
            }
            val keyCode = PlaybackStateCompat.toKeyCode(action)
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                Log.w(
                    TAG,
                    "Cannot build a media button pending intent with the given action: $action"
                )
                return null
            }
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.component = mbrComponent
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            if (Build.VERSION.SDK_INT >= 16) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
            return PendingIntent.getBroadcast(context, keyCode, intent, PendingIntent.FLAG_MUTABLE)
        }

        fun getMediaButtonReceiverComponent(context: Context): ComponentName? {
            val queryIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            queryIntent.setPackage(context.packageName)
            val pm = context.packageManager
            val resolveInfos = pm.queryBroadcastReceivers(queryIntent, 0)
            if (resolveInfos.size == 1) {
                val resolveInfo = resolveInfos[0]
                return ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
            } else if (resolveInfos.size > 1) {
                Log.w(
                    TAG,
                    "More than one BroadcastReceiver that handles " +
                        Intent.ACTION_MEDIA_BUTTON + " was found, returning null."
                )
            }
            return null
        }

        private fun getServiceComponentByAction(context: Context, action: String): ComponentName? {
            val pm = context.packageManager
            val queryIntent = Intent(action)
            queryIntent.setPackage(context.packageName)
            val resolveInfos = pm.queryIntentServices(queryIntent, 0 /* flags */)
            return if (resolveInfos.size == 1) {
                val resolveInfo = resolveInfos[0]
                ComponentName(
                    resolveInfo.serviceInfo.packageName,
                    resolveInfo.serviceInfo.name
                )
            } else if (resolveInfos.isEmpty()) {
                null
            } else {
                throw IllegalStateException(
                    "Expected 1 service that handles " + action + ", found " +
                        resolveInfos.size
                )
            }
        }
    }
}
