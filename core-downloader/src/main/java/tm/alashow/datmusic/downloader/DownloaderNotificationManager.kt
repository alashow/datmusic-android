/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tonyodev.fetch2.ACTION_TYPE_CANCEL
import com.tonyodev.fetch2.ACTION_TYPE_DELETE
import com.tonyodev.fetch2.ACTION_TYPE_INVALID
import com.tonyodev.fetch2.ACTION_TYPE_PAUSE
import com.tonyodev.fetch2.ACTION_TYPE_RESUME
import com.tonyodev.fetch2.ACTION_TYPE_RETRY
import com.tonyodev.fetch2.DefaultFetchNotificationManager
import com.tonyodev.fetch2.DownloadNotification
import com.tonyodev.fetch2.EXTRA_ACTION_TYPE
import com.tonyodev.fetch2.EXTRA_DOWNLOAD_ID
import com.tonyodev.fetch2.EXTRA_GROUP_ACTION
import com.tonyodev.fetch2.EXTRA_NAMESPACE
import com.tonyodev.fetch2.EXTRA_NOTIFICATION_GROUP_ID
import com.tonyodev.fetch2.EXTRA_NOTIFICATION_ID
import com.tonyodev.fetch2.Fetch

class DownloaderNotificationManager(val context: Context) : DefaultFetchNotificationManager(context) {
    override fun getFetchInstanceForNamespace(namespace: String): Fetch {
        return Fetch.getDefaultInstance()
    }

    override fun getActionPendingIntent(downloadNotification: DownloadNotification, actionType: DownloadNotification.ActionType): PendingIntent {
        val intent = Intent(notificationManagerAction)
        intent.putExtra(EXTRA_NAMESPACE, downloadNotification.namespace)
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadNotification.notificationId)
        intent.putExtra(EXTRA_NOTIFICATION_ID, downloadNotification.notificationId)
        intent.putExtra(EXTRA_GROUP_ACTION, false)
        intent.putExtra(EXTRA_NOTIFICATION_GROUP_ID, downloadNotification.groupId)
        val action = when (actionType) {
            DownloadNotification.ActionType.CANCEL -> ACTION_TYPE_CANCEL
            DownloadNotification.ActionType.DELETE -> ACTION_TYPE_DELETE
            DownloadNotification.ActionType.RESUME -> ACTION_TYPE_RESUME
            DownloadNotification.ActionType.PAUSE -> ACTION_TYPE_PAUSE
            DownloadNotification.ActionType.RETRY -> ACTION_TYPE_RETRY
            else -> ACTION_TYPE_INVALID
        }
        intent.putExtra(EXTRA_ACTION_TYPE, action)
        return PendingIntent.getBroadcast(
            context,
            downloadNotification.notificationId + action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // all this overriding just to add this flag
        )
    }

    override fun updateNotification(notificationBuilder: NotificationCompat.Builder, downloadNotification: DownloadNotification, context: Context) {
        super.updateNotification(
            notificationBuilder,
            downloadNotification.apply {
                title = try { // to get the file name from uri looking like title
                    title.substring(title.lastIndexOf("/") + 1)
                } catch (e: Exception) {
                    title
                }
            },
            context
        )
    }
}
