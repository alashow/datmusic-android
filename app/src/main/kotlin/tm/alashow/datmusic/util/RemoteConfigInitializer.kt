/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.util

import android.app.Application
import javax.inject.Inject
import tm.alashow.base.inititializer.AppInitializer
import tm.alashow.data.RemoteConfig

class RemoteConfigInitializer @Inject constructor(private val remoteConfig: RemoteConfig) : AppInitializer {
    override fun init(application: Application) {
        remoteConfig
    }
}
