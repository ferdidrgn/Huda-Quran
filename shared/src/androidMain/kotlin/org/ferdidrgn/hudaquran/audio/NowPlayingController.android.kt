package org.ferdidrgn.hudaquran.audio

/**
 * No-op on Android: the media notification is started directly from AudioPlayer.play()
 * via PlaybackNotificationService, which observes PlaybackManager itself.
 */
actual class NowPlayingController actual constructor(playbackManager: PlaybackManager) {
    actual fun start() {
    }
}
