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
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest

@Database(
    version = 2,
    entities = [
        Audio::class,
        Artist::class,
        Album::class,
        DownloadRequest::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(BaseTypeConverters::class, AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audiosDao(): AudiosDao
    abstract fun artistsDao(): ArtistsDao
    abstract fun albumsDao(): AlbumsDao

    abstract fun downloadRequestsDao(): DownloadRequestsDao
}
