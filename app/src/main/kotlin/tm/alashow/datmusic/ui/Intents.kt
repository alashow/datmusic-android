/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import tm.alashow.datmusic.domain.entities.ArtistId
import tm.alashow.navigation.screens.LeafScreen

fun Context.buildSearchIntent(query: String): PendingIntent {
    val intent = Intent(Intent.ACTION_VIEW, LeafScreen.Search.buildUri(query), this, MainActivity::class.java)

    return TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

fun Context.buildArtistDetailsIntent(id: ArtistId): PendingIntent {
    val intent = Intent(Intent.ACTION_VIEW, LeafScreen.ArtistDetails.buildUri(id), this, MainActivity::class.java)

    return TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
