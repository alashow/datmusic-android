/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.delegate

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import java.util.*

abstract class LocalizationActivity : ComponentActivity(), OnLocaleChangedListener {

    private val localizationDelegate = LocalizationActivityDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        localizationDelegate.onResume(this)
    }

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(localizationDelegate.attachBaseContext(newBase))

    override fun getApplicationContext() = localizationDelegate.getApplicationContext(super.getApplicationContext())

    override fun getResources() = localizationDelegate.getResources(super.getResources())

    fun setLanguage(language: String) = localizationDelegate.setLanguage(this, language)

    fun setLanguage(locale: Locale) = localizationDelegate.setLanguage(this, locale)

    fun getCurrentLanguage() = localizationDelegate.getLanguage(this)

    // Just override method locale change event
    override fun onBeforeLocaleChanged() {}

    override fun onAfterLocaleChanged() {}
}
