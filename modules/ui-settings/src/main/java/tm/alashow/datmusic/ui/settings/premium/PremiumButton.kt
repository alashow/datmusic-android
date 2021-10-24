/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.premium

import android.app.Activity
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber
import tm.alashow.base.billing.OnPermissionActive
import tm.alashow.base.billing.OnPermissionError
import tm.alashow.base.billing.Subscriptions
import tm.alashow.base.util.IntentUtils
import tm.alashow.base.util.asString
import tm.alashow.base.util.toast
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.theme.outlinedButtonColors

@Composable
fun PremiumButton(viewModel: PremiumSettingsViewModel = hiltViewModel()) {
    val premiumStatus by rememberFlowWithLifecycle(viewModel.premiumStatus).collectAsState(PremiumStatus.Unknown)
    val context = LocalContext.current

    OutlinedButton(
        enabled = premiumStatus.isActionable,
        onClick = {
            if (premiumStatus is PremiumStatus.NotSubscribed) viewModel.fakeRefresh()
            premiumStatus.handleClick(
                context = context as Activity,
                onPermissionActive = { viewModel.refreshPremiumStatus() },
                onPermissionError = { context.toast(it.toUiMessage().asString(context)) }
            )
        },
        colors = outlinedButtonColors()
    ) {
        when (premiumStatus.isLoading) {
            true -> ProgressIndicatorSmall()
            else -> Text(premiumStatus.toButtonText())
        }
    }
}

@Composable
private fun PremiumStatus.toButtonText() = when (this) {
    is PremiumStatus.NotEnabled -> stringResource(R.string.settings_premium_disabled)
    is PremiumStatus.NotSubscribed -> stringResource(R.string.settings_premium_subscribe)
    is PremiumStatus.Subscribed -> stringResource(R.string.settings_premium_view)
    else -> ""
}

private fun PremiumStatus.handleClick(
    context: Activity,
    onPermissionActive: OnPermissionActive,
    onPermissionError: OnPermissionError
) {
    when (this) {
        is PremiumStatus.NotSubscribed -> {
            Subscriptions.checkPermissions(
                context = context,
                restoreOrPurchaseOnEmpty = true,
                onPermissionActive = onPermissionActive,
                onPermissionError = onPermissionError,
            )
        }
        is PremiumStatus.Subscribed -> {
            val url = Subscriptions.getSubscriptionUrl(premiumPermission)
            IntentUtils.openUrl(context, url)
        }
        else -> Timber.e("Unhandled action: $this")
    }
}
