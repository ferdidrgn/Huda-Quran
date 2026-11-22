package org.ferdidrgn.hudaquran.automotive

import android.content.ComponentName
import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.Screen
import androidx.car.app.media.MediaPlaybackManager
import androidx.car.app.model.Header
import androidx.car.app.media.model.MediaPlaybackTemplate
import androidx.car.app.validation.HostValidator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.support.v4.media.MediaBrowserCompat
import org.ferdidrgn.hudaquran.audio.PlaybackNotificationService

/** Android for Cars entry point for the Automotive media experience. */
class HudaQuranCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator =
        HostValidator.Builder(this)
            .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
            .build()

    override fun onCreateSession(sessionInfo: SessionInfo): Session = HudaQuranCarSession()
}

private class HudaQuranCarSession : Session() {
    private var mediaBrowser: MediaBrowserCompat? = null

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> connectToPlaybackSession()
                Lifecycle.Event.ON_DESTROY -> {
                    mediaBrowser?.disconnect()
                    mediaBrowser = null
                }
                else -> Unit
            }
        })
    }

    override fun onCreateScreen(intent: Intent): Screen = HudaQuranNowPlayingScreen(carContext)

    private fun connectToPlaybackSession() {
        var connectingBrowser: MediaBrowserCompat? = null
        connectingBrowser = MediaBrowserCompat(
            carContext,
            ComponentName(carContext, PlaybackNotificationService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    val token = connectingBrowser?.sessionToken ?: return
                    val manager = carContext.getCarService(CarContext.MEDIA_PLAYBACK_SERVICE)
                        as MediaPlaybackManager
                    manager.registerMediaPlaybackToken(token)
                }
            },
            null,
        )
        mediaBrowser = connectingBrowser
        connectingBrowser.connect()
    }
}

private class HudaQuranNowPlayingScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate() = MediaPlaybackTemplate.Builder()
        .setHeader(
            Header.Builder()
                .setTitle("Hüda Kuran")
                .build(),
        )
        .build()
}
