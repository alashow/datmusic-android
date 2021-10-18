/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import tm.alashow.data.db.DatabaseTxRunner
import tm.alashow.data.db.PaginatedEntryDao
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .addMigrations(MIGRATION_3_4)
            .fallbackToDestructiveMigration()
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(db: AppDatabase): DatabaseTxRunner = DatabaseTxRunner(db)

    @Provides
    fun audiosDao(db: AppDatabase) = db.audiosDao()

    @Provides
    fun audiosDaoBase(db: AppDatabase): PaginatedEntryDao<DatmusicSearchParams, Audio> = db.audiosDao()

    @Provides
    fun artistsDao(db: AppDatabase) = db.artistsDao()

    @Provides
    fun artistsDaoBase(db: AppDatabase): PaginatedEntryDao<DatmusicSearchParams, Artist> = db.artistsDao()

    @Provides
    fun albumsDao(db: AppDatabase) = db.albumsDao()

    @Provides
    fun albumsDaoBase(db: AppDatabase): PaginatedEntryDao<DatmusicSearchParams, Album> = db.albumsDao()

    @Provides
    fun playlistsDao(db: AppDatabase) = db.playlistsDao()

    @Provides
    fun playlistsWithAudiosDao(db: AppDatabase) = db.playlistsWithAudiosDao()

    @Provides
    fun downloadRequestsDao(db: AppDatabase) = db.downloadRequestsDao()
}
