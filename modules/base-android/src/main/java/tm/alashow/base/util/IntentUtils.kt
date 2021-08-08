/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri

object IntentUtils {

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(context, intent)
        } catch (e: ActivityNotFoundException) {
            RemoteLogger.exception(e)
        }
    }

    fun openFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.setDataAndType(uri, mimeType)
            startActivity(context, intent)
        } catch (e: ActivityNotFoundException) {
            RemoteLogger.exception(e)
        }
    }

    /**
     * Open given intent as [Activity].
     * Just a wrapper so custom stuff can be done just before opening new activity.
     */
    fun startActivity(context: Context, intent: Intent, extras: Bundle? = null) {
        if (extras != null)
            context.startActivity(intent, extras)
        else
            context.startActivity(intent)
    }
}

fun Intent.clearTop() {
    flags =
        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
}
