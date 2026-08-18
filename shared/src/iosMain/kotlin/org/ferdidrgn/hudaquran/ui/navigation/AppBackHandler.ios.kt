package org.ferdidrgn.hudaquran.ui.navigation

import androidx.compose.runtime.Composable

// iOS has no hardware/system back button to intercept; navigation is gesture/UI driven only.
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {}
