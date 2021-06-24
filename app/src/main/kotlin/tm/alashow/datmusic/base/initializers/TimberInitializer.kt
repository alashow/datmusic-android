/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.base.initializers

import android.app.Application
import javax.inject.Inject
import timber.log.Timber
import tm.alashow.datmusic.BuildConfig

class TimberInitializer @Inject constructor() : AppInitializer {
    override fun init(application: Application) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
