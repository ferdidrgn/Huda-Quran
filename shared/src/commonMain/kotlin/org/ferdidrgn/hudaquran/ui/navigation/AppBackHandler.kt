package org.ferdidrgn.hudaquran.ui.navigation

import androidx.compose.runtime.Composable

/** Intercepts the platform's system back gesture/button where the platform supports it. */
@Composable
expect fun AppBackHandler(enabled: Boolean, onBack: () -> Unit)
