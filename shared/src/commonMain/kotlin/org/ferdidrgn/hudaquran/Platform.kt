package org.ferdidrgn.hudaquran

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform