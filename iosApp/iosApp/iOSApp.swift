import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        let revenueCatKey = Bundle.main.object(forInfoDictionaryKey: "RevenueCatApiKey") as? String ?? ""
        BillingManager.shared.configure(apiKey: revenueCatKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    DeepLinkBridgeKt.handleDeepLinkUrl(url: url.absoluteString)
                }
        }
    }
}