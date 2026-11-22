package org.ferdidrgn.hudaquran

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.theme.HudaQuranTheme
import org.ferdidrgn.hudaquran.ui.theme.rememberDisplayFontFamily
import org.ferdidrgn.hudaquran.ui.tv.TvHomeScreen
import org.ferdidrgn.hudaquran.ui.tv.TvPlayerScreen
import org.ferdidrgn.hudaquran.widget.LastReadWidgetProvider

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    // Full-screen, non-dismissible update flow (IMMEDIATE): the user cannot keep using the app
    // on an outdated version. If the flow gets cancelled or interrupted for any reason, this
    // requests it again rather than letting the user through.
    private val appUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) checkForMandatoryUpdate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Portrait-only on phones, Mushaf included: a landscape page on a phone is too short to
        // read, and Mushaf fits each page to the portrait frame instead. Large screens (tablets,
        // unfolded foldables, sw600dp+) are left free: Android 16+ ignores an orientation lock
        // there anyway (large-screen compatibility changes flagged by Play Console vitals), and
        // forcing portrait on that much width would waste the extra space instead of just being
        // a no-op, so we don't request it in the first place.
        if (resources.configuration.smallestScreenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        requestNotificationPermissionIfNeeded()
        handleDeepLinkIntent(intent)
        checkForMandatoryUpdate()

        setContent {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
                TvExperience()
            } else {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Covers a user backgrounding the app mid-update (e.g. to grant Play Store a permission)
        // and coming back without the update flow having finished.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    appUpdateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        LastReadWidgetProvider.refreshAll(applicationContext)
    }

    private fun checkForMandatoryUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    appUpdateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.data?.toString()?.let { DeepLinkController.handle(it) }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
private fun TvExperience() {
    val preferences = AppContainer.preferences
    val themeMode by preferences.themeMode.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    var showingPlayer by remember { mutableStateOf(false) }

    BackHandler(enabled = showingPlayer) { showingPlayer = false }

    HudaQuranTheme(
        themeMode = themeMode,
        displayFontFamily = rememberDisplayFontFamily(appLanguage),
    ) {
        if (showingPlayer) {
            TvPlayerScreen(onBack = { showingPlayer = false })
        } else {
            TvHomeScreen(onOpenPlayer = { showingPlayer = true })
        }
    }
}
