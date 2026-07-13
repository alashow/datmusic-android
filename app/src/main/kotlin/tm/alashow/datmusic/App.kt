/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic

import dagger.hilt.android.HiltAndroidApp
import tm.alashow.base.BaseApp
import tm.alashow.base.inititializer.AppInitializers
import tm.alashow.base.migrator.AppMigrator
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp() {

    @Inject
    lateinit var initializers: AppInitializers

    @Inject
    lateinit var appMigrator: AppMigrator

    override fun onCreate() {
        super.onCreate()
        initializers.init(this)
        appMigrator.migrate()
    }
}
