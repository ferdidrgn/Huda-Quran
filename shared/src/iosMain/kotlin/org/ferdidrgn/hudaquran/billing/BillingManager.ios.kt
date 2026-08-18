package org.ferdidrgn.hudaquran.billing

/**
 * StoreKit purchases aren't wired up yet. Real StoreKit calls need an NSObject-based delegate
 * implementation (SKProductsRequestDelegate / SKPaymentTransactionObserver) that this sandbox has
 * no way to compile-check without a Mac + Xcode, and the products themselves must first exist in
 * App Store Connect with matching IDs (donation_small, donation_medium, no_ads_6_months) before
 * any purchase call could work anyway. Wired as a safe no-op for now.
 */
actual object BillingManager {
    actual fun initialize() {}
    actual fun purchase(product: BillingProduct) {}
}
