/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.premium

import com.qonversion.android.sdk.dto.QPermission
import tm.alashow.base.billing.SubscriptionError

internal sealed class PremiumStatus {
    object Unknown : PremiumStatus()
    object NotEnabled : PremiumStatus()

    data class NotSubscribed(val subscriptionError: SubscriptionError) : PremiumStatus()
    data class Subscribed(val premiumPermission: QPermission) : PremiumStatus()

    val isActionable get() = this != Unknown && this != NotEnabled
    val isLoading get() = this == Unknown
}
