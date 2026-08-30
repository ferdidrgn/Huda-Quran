package org.ferdidrgn.hudaquran.billing

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.analytics.AppAnalytics
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.CurrentActivityHolder
import org.ferdidrgn.hudaquran.di.AppContainer

actual object BillingManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val handledPurchaseTokens = mutableSetOf<String>()
    private var isReady = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { handlePurchase(it) }
        }
    }

    private val client: BillingClient by lazy {
        BillingClient.newBuilder(AppContextHolder.context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
    }

    actual fun initialize() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                isReady = result.responseCode == BillingClient.BillingResponseCode.OK
            }

            override fun onBillingServiceDisconnected() {
                isReady = false
            }
        })
    }

    actual fun purchase(product: BillingProduct) {
        val activity = CurrentActivityHolder.activity ?: return
        if (!isReady) return
        scope.launch {
            val playType = if (product.type == BillingProductType.SUBSCRIPTION) {
                BillingClient.ProductType.SUBS
            } else {
                BillingClient.ProductType.INAPP
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(product.productId)
                            .setProductType(playType)
                            .build(),
                    ),
                )
                .build()
            val result = runCatching { client.queryProductDetails(params) }.getOrNull() ?: return@launch
            val details = result.productDetailsList?.firstOrNull() ?: return@launch
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
            if (product.type == BillingProductType.SUBSCRIPTION) {
                // Subscriptions need an explicit offer token identifying which base plan to buy
                // (here, the single prepaid 6-month plan configured in Play Console).
                val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@launch
                productDetailsParams.setOfferToken(offerToken)
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams.build()))
                .build()
            client.launchBillingFlow(activity, flowParams)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!handledPurchaseTokens.add(purchase.purchaseToken)) return

        if (purchase.products.contains(BillingProduct.NO_ADS_6_MONTHS.productId)) {
            AppContainer.preferences.grantAdFreePeriod(NO_ADS_GRANT_MILLIS)
        }

        purchase.products.forEach { productId ->
            AppAnalytics.logEvent("purchase_completed", mapOf("product_id" to productId))
        }

        if (!purchase.isAcknowledged) {
            val isSubscription = purchase.products.any { id ->
                BillingProduct.entries.any { it.productId == id && it.type == BillingProductType.SUBSCRIPTION }
            }
            if (isSubscription) {
                // Subscriptions are acknowledged, never consumed — consuming would strip Play's
                // record of the entitlement instead of just confirming receipt of it.
                val ackParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                client.acknowledgePurchase(ackParams) { }
            } else {
                val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                client.consumeAsync(consumeParams) { _, _ -> }
            }
        }
    }
}
