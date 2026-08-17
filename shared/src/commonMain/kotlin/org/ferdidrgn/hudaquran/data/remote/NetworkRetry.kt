package org.ferdidrgn.hudaquran.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Retries a flaky network call once after a short delay before giving up. */
suspend fun <T> retryOnce(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        delay(600)
        block()
    }
