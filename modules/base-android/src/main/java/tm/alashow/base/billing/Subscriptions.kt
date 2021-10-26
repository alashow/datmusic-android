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
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import tm.alashow.Config
import tm.alashow.base.util.toLocalDateTime

typealias OnPermissionActive = (QPermission) -> Unit
typealias OnPermissionError = (SubscriptionError) -> Unit

/**
 * Wrapper around Qonversion.
 */
object Subscriptions {
    const val KEY = ""

    private fun validateKey() {
        if (KEY.isBlank())
            throw SubscriptionsNotEnabledError
    }

    enum class Product(val id: String) {
        PremiumMonthly("premium_monthly")
    }

    enum class Permission(val id: String) {
        Premium("Premium")
    }

    fun getSubscriptionUrl(product: QPermission) =
        "https://play.google.com/store/account/subscriptions?sku=${product.productID}&package=${Config.PLAYSTORE_ID}"

    private fun QPermission.expiresAt() = (expirationDate ?: Date()).toLocalDateTime()
    private fun QPermission.isExpired() = LocalDateTime.now() >= expiresAt()
    private fun QPermission.isActiveAndNotExpired() = isActive() && isExpired().not()

    /**
     * @param restoreOrPurchaseOnEmpty tries to restore or make a purchase if can't be restored in case there's no permissions and this is set to true
     */
    fun checkPermissions(
        context: Activity,
        permission: Permission = Permission.Premium,
        product: Product = Product.PremiumMonthly,
        restoreOrPurchaseOnEmpty: Boolean = false,
        onPermissionActive: OnPermissionActive = {},
        onPermissionError: OnPermissionError = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null && premiumPermission.isActiveAndNotExpired()) {
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
        onPermissionActive: OnPermissionActive = {},
        onPermissionError: OnPermissionError = { Timber.e(it) },
    ) {
        validateKey()
        val onRestoreFail = {
            if (purchaseIfNotOwned) {
                Timber.d("Cannot restore purchase, trying to purchase..")
                makePurchase(context, product, permission, onPermissionActive, onPermissionError)
            }
        }
        Qonversion.restore(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null) {
                    if (premiumPermission.isActive()) {
                        Timber.d("Permission restored: $permission")
                        onPermissionActive(premiumPermission)
                    } else onRestoreFail()
                } else onRestoreFail()
            }

            override fun onError(error: QonversionError) {
                if (error.code == QonversionErrorCode.ProductNotOwned) onRestoreFail()
                else onPermissionError(SubscriptionError(error))
            }
        })
    }

    fun makePurchase(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        permission: Permission = Permission.Premium,
        onPermissionActive: OnPermissionActive = {},
        onPermissionError: OnPermissionError = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.purchase(
            context, product.id,
            callback = object : QonversionPermissionsCallback {
                override fun onSuccess(permissions: Map<String, QPermission>) {
                    val premiumPermission = permissions[permission.id]
                    if (premiumPermission != null && premiumPermission.isActive()) {
                        onPermissionActive(premiumPermission)
                    } else onPermissionError(SubscriptionNoPermissionsError)
                }

                override fun onError(error: QonversionError) {
                    onPermissionError(SubscriptionError(error))
                }
            }
        )
    }

    suspend fun checkPremiumPermission(permission: Permission = Permission.Premium): QPermission = suspendCoroutine { continuation ->
        validateKey()
        Timber.d("Checking for permission=$permission")
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                Timber.d("Has permission: $premiumPermission")
                if (premiumPermission != null && premiumPermission.isActiveAndNotExpired()) {
                    continuation.resume(premiumPermission)
                } else continuation.resumeWithException(SubscriptionNoPermissionsError)
            }

            override fun onError(error: QonversionError) {
                continuation.resumeWithException(SubscriptionError(error))
            }
        })
    }
}
