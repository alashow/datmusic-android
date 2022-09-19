/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tm.alashow.base.billing.SubscriptionError
import tm.alashow.base.billing.Subscriptions
import tm.alashow.base.billing.SubscriptionsNotEnabledError
import tm.alashow.base.util.extensions.stateInDefault

@HiltViewModel
internal class PremiumSettingsViewModel @Inject constructor() : ViewModel() {

    private val premiumStatusState = MutableStateFlow<PremiumStatus>(PremiumStatus.Unknown)
    val premiumStatus = premiumStatusState.stateInDefault(viewModelScope, PremiumStatus.Unknown)

    init {
        refreshPremiumStatus()
    }

    fun refreshPremiumStatus() {
        viewModelScope.launch {
            try {
                premiumStatusState.value = PremiumStatus.Subscribed(Subscriptions.checkPremiumPermission())
            } catch (error: SubscriptionError) {
                premiumStatusState.value = PremiumStatus.NotSubscribed(error)
            } catch (e: SubscriptionsNotEnabledError) {
                premiumStatusState.value = PremiumStatus.NotEnabled
            }
        }
    }

    fun fakeRefresh(delayMillis: Long = 3500L) {
        viewModelScope.launch {
            premiumStatusState.value = PremiumStatus.Unknown
            delay(delayMillis)
            refreshPremiumStatus()
        }
    }
}
