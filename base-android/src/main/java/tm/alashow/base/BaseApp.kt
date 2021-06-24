/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import tm.alashow.base.util.Locales

/**
 * Just a wrapper app class to have base stuff.
 */
abstract class BaseApp : Application() {

    private var localizationDelegate = LocalizationApplicationDelegate()

    override fun attachBaseContext(base: Context) {
        localizationDelegate.setDefaultLanguage(base, Locales.TK)
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localizationDelegate.onConfigurationChanged(this)
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }
}
