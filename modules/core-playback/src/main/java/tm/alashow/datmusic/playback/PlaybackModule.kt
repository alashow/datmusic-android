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
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.players.AudioPlayerImpl
import tm.alashow.datmusic.playback.services.PlayerService

@InstallIn(SingletonComponent::class)
@Module
class PlaybackModule {

    @Provides
    @Singleton
    fun playbackConnection(
        @ApplicationContext context: Context,
        audiosRepo: AudiosRepo,
        audioPlayer: AudioPlayerImpl,
        downloader: Downloader,
        snackbarManager: SnackbarManager,
    ): PlaybackConnection = PlaybackConnectionImpl(
        context = context,
        serviceComponent = ComponentName(context, PlayerService::class.java),
        audiosRepo = audiosRepo,
        audioPlayer = audioPlayer,
        downloader = downloader,
        snackbarManager = snackbarManager,
    )
}
