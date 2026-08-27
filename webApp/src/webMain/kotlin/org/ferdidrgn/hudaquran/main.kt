package org.ferdidrgn.hudaquran

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    DeepLinkController.handle(currentBrowserUrl())
    // Mounts into the #compose-target div from index.html, rather than appending a canvas
    // straight onto <body>, so the real SEO header/footer markup around it stays intact — see
    // the comment in index.html for why that markup needs to exist at all.
    ComposeViewport(viewportContainerId = "compose-target") {
        App()
    }
}

private fun currentBrowserUrl(): String = js("window.location.pathname + window.location.search")
