/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.billing

import android.app.Activity
import android.content.Context
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.QonversionErrorCode
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionProductsCallback
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import tm.alashow.Config
import tm.alashow.base.util.asString
import tm.alashow.base.util.toLocalDateTime
import tm.alashow.base.util.toUiMessage
import tm.alashow.base.util.toast

typealias OnEntitlementActive = (QEntitlement) -> Unit
typealias OnEntitlementError = (SubscriptionError) -> Unit

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

    enum class Entitlement(val id: String) {
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
    fun checkEntitlements(
        context: Activity,
        entitlement: Entitlement = Entitlement.Premium,
        product: Product = Product.PremiumMonthly,
        restoreOrPurchaseOnEmpty: Boolean = false,
        onEntitlementActive: OnEntitlementActive = {},
        onEntitlementError: OnEntitlementError = { Timber.e(it) },
    ) {
        validateKey()
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumEntitlement = entitlements[entitlement.id]
                if (premiumEntitlement != null && premiumEntitlement.isActiveAndNotExpired()) {
                    Timber.d("Has entitlement: $entitlement")
                    onEntitlementActive(premiumEntitlement)
                } else if (restoreOrPurchaseOnEmpty) {
                    Timber.d("Has no entitlement: $entitlement, trying to restore..")
                    restoreEntitlement(context, product, entitlement, true, onEntitlementActive, onEntitlementError)
                } else onEntitlementError(SubscriptionNoPermissionsError)
            }

            override fun onError(error: QonversionError) {
                onEntitlementError(SubscriptionError(error))
            }
        })
    }

    fun restoreEntitlement(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        entitlement: Entitlement = Entitlement.Premium,
        purchaseIfNotOwned: Boolean = false,
        onEntitlementActive: OnEntitlementActive = {},
        onEntitlementError: OnEntitlementError = { Timber.e(it) },
    ) {
        validateKey()
        val onRestoreFail = {
            if (purchaseIfNotOwned) {
                Timber.d("Cannot restore purchase, trying to purchase..")
                makePurchase(context, product, entitlement, onEntitlementActive, onEntitlementError)
            }
        }
        Qonversion.shared.restore(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumPermission = entitlements[entitlement.id]
                if (premiumPermission != null) {
                    if (premiumPermission.isActive) {
                        Timber.d("Permission restored: $entitlement")
                        onEntitlementActive(premiumPermission)
                    } else onRestoreFail()
                } else onRestoreFail()
            }

            override fun onError(error: QonversionError) {
                if (error.code == QonversionErrorCode.ProductNotOwned) onRestoreFail()
                else onEntitlementError(SubscriptionError(error))
            }
        })
    }

    fun makePurchase(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        entitlement: Entitlement = Entitlement.Premium,
        onEntitlementActive: OnEntitlementActive = {},
        onEntitlementError: OnEntitlementError = { Timber.e(it) },
    ) = scope.launch {
        validateKey()
        val qProduct = resolveQProduct(product.id) ?: return@launch
        Qonversion.shared.purchase(
            context, qProduct,
            callback = object : QonversionEntitlementsCallback {
                override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                    val premiumPermission = entitlements[entitlement.id]
                    if (premiumPermission != null && premiumPermission.isActive) {
                        onEntitlementActive(premiumPermission)
                    } else onEntitlementError(SubscriptionNoPermissionsError)
                }

                override fun onError(error: QonversionError) {
                    onEntitlementError(SubscriptionError(error))
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

    suspend fun validatePremiumEntitlement(): QEntitlement = validateEntitlement(Entitlement.Premium)

    suspend fun validateEntitlement(
        context: Context,
        entitlement: Entitlement = Entitlement.Premium,
        onError: suspend (Throwable) -> Unit = { context.toast(it.toUiMessage().asString(context)) },
        onActive: suspend (QEntitlement) -> Unit,
    ) {
        runCatching { validateEntitlement(entitlement) }
            .onSuccess { onActive(it) }
            .onFailure { onError(it) }
    }

    suspend fun validateEntitlement(entitlement: Entitlement): QEntitlement = suspendCancellableCoroutine { continuation ->
        validateKey()
        Timber.d("Checking for permission=$entitlement")
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premiumPermission = entitlements[entitlement.id]
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
