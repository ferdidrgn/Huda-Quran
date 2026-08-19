package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Coarse breakpoints for adapting the app's chrome and content width to the available window,
 * covering the phone / tablet / desktop split web and desktop targets need. Mirrors Material's
 * compact/medium/expanded window size classes without pulling in the Android-only
 * `material3-window-size-class` artifact (this needs to work on web and iOS too).
 */
enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

private val mediumBreakpoint = 600.dp
private val expandedBreakpoint = 1200.dp

fun windowSizeClassOf(width: Dp): WindowSizeClass = when {
    width < mediumBreakpoint -> WindowSizeClass.COMPACT
    width < expandedBreakpoint -> WindowSizeClass.MEDIUM
    else -> WindowSizeClass.EXPANDED
}
