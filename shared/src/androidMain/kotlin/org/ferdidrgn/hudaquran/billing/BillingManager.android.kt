package org.ferdidrgn.hudaquran.billing

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
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(product.productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    ),
                )
                .build()
            val result = runCatching { client.queryProductDetails(params) }.getOrNull() ?: return@launch
            val details = result.productDetailsList?.firstOrNull() ?: return@launch
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build(),
                    ),
                )
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
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            client.consumeAsync(consumeParams) { _, _ -> }
        }
    }
}
