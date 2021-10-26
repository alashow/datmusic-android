/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.billing

import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionErrorCode
import tm.alashow.baseAndroid.R
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.UiMessageConvertable

open class SubscriptionError(val qonversionError: QonversionError) : Throwable(), UiMessageConvertable {
    override fun toUiMessage(): UiMessage<*> = UiMessage.Plain(qonversionError.description)

    override fun toString() = qonversionError.toString()
}

object SubscriptionNoPermissionsError : SubscriptionError(QonversionError(QonversionErrorCode.ProductNotOwned)) {
    override fun toUiMessage() = UiMessage.Resource(R.string.subscriptions_required)
}

object SubscriptionsNotEnabledError : Throwable(message = "Subscriptions not enabled")
