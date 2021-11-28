/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tm.alashow.base.di.TestNetworkModule
import tm.alashow.datmusic.data.db.TestDatabaseModule

@Module(includes = [TestNetworkModule::class, TestDatabaseModule::class])
@InstallIn(SingletonComponent::class)
class TestModule
