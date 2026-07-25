/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.billing

import android.app.Application
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionConfig
import com.qonversion.android.sdk.dto.QEnvironment
import com.qonversion.android.sdk.dto.QLaunchMode
import com.qonversion.android.sdk.dto.properties.QUserPropertyKey
import tm.alashow.base.inititializer.AppInitializer
import tm.alashow.base.util.extensions.androidId
import tm.alashow.baseAndroid.BuildConfig
import javax.inject.Inject


class SubscriptionsInitializer @Inject constructor() : AppInitializer {
    override fun init(application: Application) {
        if (Subscriptions.KEY.isNotBlank()) {
            val qonversionConfigBuilder = QonversionConfig.Builder(
                context = application,
                projectKey = Subscriptions.KEY,
                launchMode = QLaunchMode.SubscriptionManagement
            ).setEnvironment(
                when {
                    BuildConfig.DEBUG -> QEnvironment.Sandbox
                    else -> QEnvironment.Production
                }
            )
            Qonversion.initialize(qonversionConfigBuilder.build())
            Qonversion.shared.setUserProperty(QUserPropertyKey.CustomUserId, application.androidId())
        }
    }
}
