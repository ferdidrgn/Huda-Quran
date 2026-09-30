package org.ferdidrgn.hudaquran.billing

actual object BillingManager {
    actual val isSupported: Boolean = false
    actual fun configure(apiKey: String) = Unit
    actual suspend fun purchase(product: BillingProduct): PurchaseOutcome = PurchaseOutcome.UNAVAILABLE
    actual suspend fun restore(): PurchaseOutcome = PurchaseOutcome.UNAVAILABLE
    actual suspend fun refresh() = Unit
}
