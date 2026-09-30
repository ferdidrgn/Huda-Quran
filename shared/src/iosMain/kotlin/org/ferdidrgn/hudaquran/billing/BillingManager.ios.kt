package org.ferdidrgn.hudaquran.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitGetProducts
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlinx.coroutines.CancellationException
import org.ferdidrgn.hudaquran.analytics.AppAnalytics
import org.ferdidrgn.hudaquran.di.AppContainer

// Shared RevenueCat implementation (identical on Android and iOS; the KMP SDK has no web target).
actual object BillingManager {
    actual val isSupported: Boolean get() = Purchases.isConfigured

    actual fun configure(apiKey: String) {
        if (apiKey.isBlank() || Purchases.isConfigured) return
        Purchases.logLevel = LogLevel.WARN
        Purchases.configure(apiKey = apiKey) { }
    }

    actual suspend fun purchase(product: BillingProduct): PurchaseOutcome {
        if (!Purchases.isConfigured) return PurchaseOutcome.UNAVAILABLE
        val purchases = Purchases.sharedInstance
        return try {
            val result = if (product == BillingProduct.NO_ADS_6_MONTHS) {
                val offering = purchases.awaitOfferings().current
                val pkg = offering?.sixMonth ?: offering?.availablePackages?.firstOrNull()
                    ?: return PurchaseOutcome.UNAVAILABLE
                purchases.awaitPurchase(pkg)
            } else {
                val storeProduct = purchases.awaitGetProducts(listOf(product.productId)).firstOrNull()
                    ?: return PurchaseOutcome.UNAVAILABLE
                purchases.awaitPurchase(storeProduct)
            }
            applyCustomerInfo(result.customerInfo)
            AppAnalytics.logEvent("purchase_completed", mapOf("product_id" to product.productId))
            PurchaseOutcome.SUCCESS
        } catch (e: CancellationException) {
            throw e
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) PurchaseOutcome.CANCELLED else PurchaseOutcome.ERROR
        } catch (e: Exception) {
            PurchaseOutcome.ERROR
        }
    }

    actual suspend fun restore(): PurchaseOutcome {
        if (!Purchases.isConfigured) return PurchaseOutcome.UNAVAILABLE
        return try {
            val info = Purchases.sharedInstance.awaitRestore()
            applyCustomerInfo(info)
            if (info.entitlements[AD_FREE_ENTITLEMENT]?.isActive == true) PurchaseOutcome.SUCCESS else PurchaseOutcome.NOT_ACTIVE
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            PurchaseOutcome.ERROR
        }
    }

    actual suspend fun refresh() {
        if (!Purchases.isConfigured) return
        try {
            applyCustomerInfo(Purchases.sharedInstance.awaitCustomerInfo())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Offline: keep the last known expiry.
        }
    }

    /** Mirrors the entitlement's expiry locally so ads stay off offline until it lapses. */
    private fun applyCustomerInfo(info: CustomerInfo) {
        val entitlement = info.entitlements[AD_FREE_ENTITLEMENT]
        val until = if (entitlement?.isActive == true) entitlement.expirationDateMillis ?: Long.MAX_VALUE / 2 else 0L
        AppContainer.preferences.setAdFreeUntil(until)
    }
}
