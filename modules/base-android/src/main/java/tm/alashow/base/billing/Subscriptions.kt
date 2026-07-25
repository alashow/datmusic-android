/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.billing

import android.app.Activity
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.QonversionErrorCode
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionProductsCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import tm.alashow.Config
import tm.alashow.base.util.toLocalDateTime
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

typealias OnPermissionActive = (QEntitlement) -> Unit
typealias OnPermissionError = (SubscriptionError) -> Unit

/**
 * Wrapper around Qonversion.
 */
object Subscriptions {
    const val KEY = ""

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    fun getSubscriptionUrl(product: QEntitlement) =
        "https://play.google.com/store/account/subscriptions?sku=${product.productId}&package=${Config.PLAYSTORE_ID}"

    private fun QEntitlement.expiresAt() = (expirationDate ?: Date()).toLocalDateTime()
    private fun QEntitlement.isExpired() = LocalDateTime.now() >= expiresAt()
    private fun QEntitlement.isActiveAndNotExpired() = isActive && isExpired().not()

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
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumPermission = entitlements[permission.id]
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
        Qonversion.shared.restore(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumPermission = entitlements[permission.id]
                if (premiumPermission != null) {
                    if (premiumPermission.isActive) {
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
    ) = scope.launch {
        validateKey()
        val qProduct = resolveQProduct(product.id) ?: return@launch
        Qonversion.shared.purchase(
            context, qProduct,
            callback = object : QonversionEntitlementsCallback {
                override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                    val premiumPermission = entitlements[permission.id]
                    if (premiumPermission != null && premiumPermission.isActive) {
                        onPermissionActive(premiumPermission)
                    } else onPermissionError(SubscriptionNoPermissionsError)
                }

                override fun onError(error: QonversionError) {
                    onPermissionError(SubscriptionError(error))
                }
            }
        )
    }

    private suspend fun resolveQProduct(id: String): QProduct? = suspendCancellableCoroutine { continuation ->
        Qonversion.shared.products(object : QonversionProductsCallback {
            override fun onError(error: QonversionError) {
                return continuation.resumeWithException(SubscriptionError(error))
            }

            override fun onSuccess(products: Map<String, QProduct>) {
                continuation.resume(products[id])
            }
        })
    }

    suspend fun checkPremiumPermission(permission: Permission = Permission.Premium): QEntitlement = suspendCancellableCoroutine { continuation ->
        validateKey()
        Timber.d("Checking for permission=$permission")
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumPermission = entitlements[permission.id]
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
