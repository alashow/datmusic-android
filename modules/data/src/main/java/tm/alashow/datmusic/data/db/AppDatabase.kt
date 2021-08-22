/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tm.alashow.data.db.BaseTypeConverters
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudio

@Database(
    version = 3,
    entities = [
        Audio::class,
        Artist::class,
        Album::class,
        DownloadRequest::class,
        Playlist::class,
        PlaylistAudio::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(BaseTypeConverters::class, AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audiosDao(): AudiosDao
    abstract fun artistsDao(): ArtistsDao
    abstract fun albumsDao(): AlbumsDao

    abstract fun playlistsDao(): PlaylistsDao
    abstract fun playlistsWithAudiosDao(): PlaylistsWithAudiosDao

    abstract fun downloadRequestsDao(): DownloadRequestsDao
}
