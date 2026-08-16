package org.ferdidrgn.hudaquran.audio

/**
 * Wires platform "Now Playing" surfaces (Android media notification, iOS lock screen /
 * Control Center) to the shared PlaybackManager. Call start() once, near app launch.
 */
expect class NowPlayingController(playbackManager: PlaybackManager) {
    fun start()
}
