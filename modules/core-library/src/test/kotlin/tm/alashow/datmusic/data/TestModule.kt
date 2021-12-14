/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tm.alashow.base.di.TestAppModule
import tm.alashow.base.testing.TestImageModule
import tm.alashow.datmusic.data.db.TestDatabaseModule

@Module(includes = [TestAppModule::class, TestDatabaseModule::class, TestImageModule::class])
@InstallIn(SingletonComponent::class)
class TestModule
