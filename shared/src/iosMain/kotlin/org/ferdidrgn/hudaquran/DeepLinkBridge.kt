package org.ferdidrgn.hudaquran

import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController

/**
 * Callable from Swift (exposed as `DeepLinkBridgeKt.handleDeepLinkUrl(url:)`) from
 * `WindowGroup { ... }.onOpenURL { url in ... }` in iOSApp.swift, for both the custom
 * `hudaquran://` scheme (registered via CFBundleURLTypes in Info.plist — works out of the box)
 * and, once set up, Universal Links (`https://hudaquran.web.app/...`).
 *
 * Universal Links additionally need the Associated Domains capability enabled on the app target
 * in Xcode (with a paid Apple Developer Team) and an `apple-app-site-association` file hosted at
 * `https://hudaquran.web.app/.well-known/apple-app-site-association` — that half can't be done
 * from here since it requires Xcode project/signing settings and an Apple Developer account this
 * environment has no access to verify against.
 */
fun handleDeepLinkUrl(url: String) {
    DeepLinkController.handle(url)
}
