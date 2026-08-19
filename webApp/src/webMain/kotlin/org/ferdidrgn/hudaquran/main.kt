package org.ferdidrgn.hudaquran

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    DeepLinkController.handle(currentBrowserUrl())
    ComposeViewport {
        App()
    }
}

private fun currentBrowserUrl(): String = js("window.location.pathname + window.location.search")
