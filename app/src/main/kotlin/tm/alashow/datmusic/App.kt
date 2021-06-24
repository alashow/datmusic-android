/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic

import android.content.Context
import androidx.multidex.MultiDex
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import tm.alashow.base.BaseApp
import tm.alashow.datmusic.base.initializers.AppInitializers

@HiltAndroidApp
class App : BaseApp() {

    @Inject
    lateinit var initializers: AppInitializers

    override fun onCreate() {
        super.onCreate()
        // Enable LeakCanary only sometimes because of all weird system leaks
        //        // Return if this process is dedicated to LeakCanary for heap analysis.
        //        if (LeakCanary.isInAnalyzerProcess(this)) {
        //            return
        //        }
        //        LeakCanary.install(this)
        initializers.init(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
