/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tm.alashow.datmusic.data.db.TestDatabaseModule

@Module(includes = [TestDatabaseModule::class])
@InstallIn(SingletonComponent::class)
object TestModule
