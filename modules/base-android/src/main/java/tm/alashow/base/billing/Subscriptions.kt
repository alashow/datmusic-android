/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.billing

import android.app.Activity
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionErrorCode
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import timber.log.Timber
import tm.alashow.baseAndroid.R
import tm.alashow.i18n.UiMessage

open class SubscriptionError(val qonversionError: QonversionError) : Throwable() {
    open fun toUiMessage(): UiMessage<*> = UiMessage.Plain(qonversionError.additionalMessage)

    override fun toString() = "QonversionError: description=${qonversionError.description}, message=${qonversionError.additionalMessage}"
}

object SubscriptionNoPermissionsError : SubscriptionError(QonversionError(QonversionErrorCode.ProductNotOwned)) {
    override fun toUiMessage() = UiMessage.Resource(R.string.subscriptions_required)
}

/**
 * Wrapper around Qonversion.
 */
object Subscriptions {
    const val KEY = ""

    fun validateKey() {
        if (KEY.isBlank())
            error("Subscriptions Key isn't set")
    }

    enum class Product(val id: String) {
        PremiumMonthly("premium_monthly")
    }

    enum class Permission(val id: String) {
        Premium("Premium")
    }

    /**
     * @param restoreOrPurchaseOnEmpty tries to restore or make a purchase if can't be restored in case there's no permissions and this is set to true
     */
    fun checkPermissions(
        context: Activity,
        permission: Permission = Permission.Premium,
        product: Product = Product.PremiumMonthly,
        restoreOrPurchaseOnEmpty: Boolean = false,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (SubscriptionError) -> Unit = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null && premiumPermission.isActive()) {
                    Timber.d("Has permission: $permission")
                    onPermissionActive(premiumPermission)
                } else if (restoreOrPurchaseOnEmpty) {
                    Timber.d("Has no permission: $permission, trying to restore..")
                    restorePermissions(context, product, permission, true, onPermissionActive, onPermissionError)
                } else onPermissionError(SubscriptionNoPermissionsError)
            }

            override fun onError(error: QonversionError) {
                onPermissionError(SubscriptionError(error))
            }
        })
    }

    fun restorePermissions(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        permission: Permission = Permission.Premium,
        purchaseIfNotOwned: Boolean = false,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (SubscriptionError) -> Unit = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.restore(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null && premiumPermission.isActive()) {
                    Timber.d("Permission restored: $permission")
                    onPermissionActive(premiumPermission)
                }
            }

            override fun onError(error: QonversionError) {
                if (error.code == QonversionErrorCode.ProductNotOwned) {
                    if (purchaseIfNotOwned) {
                        makePurchase(context, product, permission, onPermissionActive, onPermissionError)
                        return
                    }
                }
                onPermissionError(SubscriptionError(error))
            }
        })
    }

    fun makePurchase(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        permission: Permission = Permission.Premium,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (SubscriptionError) -> Unit = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.purchase(
            context, product.id,
            callback = object : QonversionPermissionsCallback {
                override fun onSuccess(permissions: Map<String, QPermission>) {
                    val premiumPermission = permissions[permission.id]
                    if (premiumPermission != null && premiumPermission.isActive()) {
                        onPermissionActive(premiumPermission)
                    }
                }

                override fun onError(error: QonversionError) {
                    onPermissionError(SubscriptionError(error))
                }
            }
        )
    }

    suspend fun checkPremiumPermission(permission: Permission = Permission.Premium): Any = suspendCoroutine { continuation ->
        validateKey()
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null && premiumPermission.isActive()) {
                    continuation.resume(premiumPermission)
                } else continuation.resumeWithException(SubscriptionNoPermissionsError)
            }

            override fun onError(error: QonversionError) {
                continuation.resumeWithException(SubscriptionError(error))
            }
        })
    }
}
