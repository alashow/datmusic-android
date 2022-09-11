/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tm.alashow.base.di.TestAppModule

@Module(includes = [TestAppModule::class])
@InstallIn(SingletonComponent::class)
class TestModule
