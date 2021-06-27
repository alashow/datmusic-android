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

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration()
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(db: AppDatabase): DatabaseTxRunner = DatabaseTxRunner(db)

    @Provides
    fun audiosDao(db: AppDatabase) = db.audiosDao()
}
