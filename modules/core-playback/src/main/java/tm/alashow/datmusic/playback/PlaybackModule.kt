/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.content.ComponentName
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.playback.players.AudioPlayerImpl
import tm.alashow.datmusic.playback.services.PlayerService

@InstallIn(SingletonComponent::class)
@Module
class PlaybackModule {

    @Provides
    @Singleton
    fun playbackConnection(
        @ApplicationContext context: Context,
        audiosDao: AudiosDao,
        downloadsRequestsDao: DownloadRequestsDao,
        audioPlayer: AudioPlayerImpl,
    ): PlaybackConnection = PlaybackConnectionImpl(
        context = context,
        serviceComponent = ComponentName(context, PlayerService::class.java),
        audiosDao = audiosDao,
        downloadsDao = downloadsRequestsDao,
        audioPlayer = audioPlayer,
    )
}
