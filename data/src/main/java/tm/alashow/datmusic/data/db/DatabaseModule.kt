/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
//    @Singleton
//    @Provides
//    fun provideDatabase(context: Context): AppDatabase {
//        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
//            .fallbackToDestructiveMigration()
//        return builder.build()
//    }

//    @Singleton
//    @Provides
//    fun provideDatabaseTransactionRunner(db: AppDatabase): DatabaseTxRunner = DatabaseTxRunner(db)

//    @Provides
//    fun provideXDao(db: AppDatabase) = db.xDao()
}
