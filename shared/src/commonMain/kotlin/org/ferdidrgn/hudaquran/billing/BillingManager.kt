package org.ferdidrgn.hudaquran.billing

enum class BillingProductType { ONE_TIME, SUBSCRIPTION }

/**
 * Store product IDs. They must exist with exactly these IDs in Play Console / App Store Connect
 * and be imported into RevenueCat. Play subscription IDs allow only lowercase letters, digits, `_`
 * and `.` — the old "no-ads-6-mounths" could never be created, which is why the button did nothing.
 */
enum class BillingProduct(val productId: String, val type: BillingProductType) {
    DONATION_SMALL("donation_small", BillingProductType.ONE_TIME),
    DONATION_MEDIUM("donation_medium", BillingProductType.ONE_TIME),
    NO_ADS_6_MONTHS("no_ads_6_months", BillingProductType.SUBSCRIPTION),
}

/** RevenueCat entitlement that turns ads off while active. */
const val AD_FREE_ENTITLEMENT = "ad_free"

enum class PurchaseOutcome { SUCCESS, CANCELLED, NOT_ACTIVE, UNAVAILABLE, ERROR }

/**
 * Purchases through RevenueCat on Android and iOS (no-op on web, where [isSupported] is false).
 *
 * "6 months without ads" is a 6-month auto-renewing subscription attached to the [AD_FREE_ENTITLEMENT]
 * entitlement: RevenueCat tracks renewal, cancellation, refunds and expiry server-side, restores it
 * on a new device or reinstall, and the app mirrors the entitlement's expiry into
 * AppPreferences so ads stay off offline too.
 */
expect object BillingManager {
    val isSupported: Boolean

    /** Called once at app start with the platform's public RevenueCat SDK key; blank = disabled. */
    fun configure(apiKey: String)

    suspend fun purchase(product: BillingProduct): PurchaseOutcome

    /** Re-fetches purchases from the store account ("Restore purchases"). */
    suspend fun restore(): PurchaseOutcome

    /** Syncs the current entitlement state (app start, returning to Settings). */
    suspend fun refresh()
}
