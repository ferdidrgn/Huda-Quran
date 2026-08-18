package org.ferdidrgn.hudaquran.billing

enum class BillingProduct(val productId: String) {
    DONATION_SMALL("donation_small"),
    DONATION_MEDIUM("donation_medium"),
    NO_ADS_6_MONTHS("no_ads_6_months"),
}

const val NO_ADS_GRANT_MILLIS: Long = 1000L * 60 * 60 * 24 * 30 * 6 // ~6 months

/**
 * Wraps the platform billing SDK for one-time purchases: two donation tiers and a one-time
 * "6 months ad-free" unlock. These product IDs must be created in Play Console / App Store
 * Connect with exactly these IDs before real purchases will work — they don't exist anywhere
 * until you create them there.
 */
expect object BillingManager {
    fun initialize()
    fun purchase(product: BillingProduct)
}
