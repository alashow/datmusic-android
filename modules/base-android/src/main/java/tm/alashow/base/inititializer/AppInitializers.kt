/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.inititializer

import android.app.Application

class AppInitializers(private vararg val initializers: AppInitializer) : AppInitializer {
    override fun init(application: Application) {
        initializers.forEach {
            it.init(application)
        }
    }
}

interface AppInitializer {
    fun init(application: Application)
}
