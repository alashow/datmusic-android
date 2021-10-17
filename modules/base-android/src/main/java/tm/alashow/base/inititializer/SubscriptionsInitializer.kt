/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.inititializer

import android.app.Activity
import android.app.Application
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionErrorCode
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import javax.inject.Inject
import timber.log.Timber
import tm.alashow.base.ui.utils.extensions.androidId

class SubscriptionsInitializer @Inject constructor() : AppInitializer {
    private val key = ""
    override fun init(application: Application) {
        if (key.isNotBlank()) {
            Qonversion.launch(application, key, false)
            Qonversion.setUserID(application.androidId())
        }
    }
}

object Subscriptions {
    enum class Product(val id: String) {
        PremiumMonthly("premium_monthly")
    }

    enum class Permission(val id: String) {
        Premium("Premium")
    }

    fun checkPermissions(
        context: Activity,
        permission: Permission = Permission.Premium,
        product: Product = Product.PremiumMonthly,
        restoreOrPurchaseOnEmpty: Boolean = false,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (QonversionError) -> Unit = { Timber.e(it.description) },
    ) {
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onSuccess(permissions: Map<String, QPermission>) {
                val premiumPermission = permissions[permission.id]
                if (premiumPermission != null && premiumPermission.isActive()) {
                    Timber.d("Has permission: $permission")
                    onPermissionActive(premiumPermission)
                } else if (restoreOrPurchaseOnEmpty) {
                    Timber.d("Has no permission: $permission, trying to restore..")
                    restorePermissions(context, product, permission, true, onPermissionActive, onPermissionError)
                } else onPermissionError(QonversionError(QonversionErrorCode.ProductNotOwned))
            }

            override fun onError(error: QonversionError) {
                onPermissionError(error)
            }
        })
    }

    fun restorePermissions(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        permission: Permission = Permission.Premium,
        purchaseIfNotOwned: Boolean = false,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (QonversionError) -> Unit = { Timber.e(it.description) },
    ) {
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
                onPermissionError(error)
            }
        })
    }

    fun makePurchase(
        context: Activity,
        product: Product = Product.PremiumMonthly,
        permission: Permission = Permission.Premium,
        onPermissionActive: (QPermission) -> Unit = {},
        onPermissionError: (QonversionError) -> Unit = { Timber.e(it.description) },
    ) {
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
                    onPermissionError(error)
                }
            }
        )
    }
}
