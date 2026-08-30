package org.ferdidrgn.hudaquran.billing

enum class BillingProductType { ONE_TIME, SUBSCRIPTION }

enum class BillingProduct(val productId: String, val type: BillingProductType) {
    DONATION_SMALL("donation_small", BillingProductType.ONE_TIME),
    DONATION_MEDIUM("donation_medium", BillingProductType.ONE_TIME),
    NO_ADS_6_MONTHS("no-ads-6-mounths", BillingProductType.SUBSCRIPTION),
}

const val NO_ADS_GRANT_MILLIS: Long = 1000L * 60 * 60 * 24 * 30 * 6 // ~6 months

/**
 * Wraps the platform billing SDK: two one-time donation tiers, plus a "6 months ad-free"
 * subscription (a prepaid, non-auto-renewing base plan in Play Console — the user re-purchases
 * when it lapses rather than being billed automatically). These product IDs must be created in
 * Play Console / App Store Connect with exactly these IDs before real purchases will work — they
 * don't exist anywhere until you create them there.
 */
expect object BillingManager {
    fun initialize()
    fun purchase(product: BillingProduct)
}
