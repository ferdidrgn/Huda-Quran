package org.ferdidrgn.hudaquran

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (!shouldMountCompose()) return
    markComposeReady()
    DeepLinkController.handle(currentBrowserUrl())
    ComposeViewport(viewportContainerId = "compose-target") {
        App()
    }
}

private fun currentBrowserUrl(): String = js("(()=>{const p=new URLSearchParams(window.location.search);const s=p.get('screen');const h=window.location.hash;return s ? '/'+s : h.startsWith('#/') ? h.substring(1) : window.location.pathname+window.location.search})()")

private fun shouldMountCompose(): Boolean = js("window.location.pathname !== '/' || new URLSearchParams(window.location.search).has('app') || window.location.hash.startsWith('#/')")

private fun markComposeReady(): Unit = js("(()=>{document.documentElement.classList.add('app-mode');document.documentElement.classList.add('compose-ready')})()")
