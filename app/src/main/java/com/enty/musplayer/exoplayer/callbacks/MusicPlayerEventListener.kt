package com.enty.musplayer.exoplayer.callbacks

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.enty.musplayer.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
): Player.EventListener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (!playWhenReady && playbackState == Player.STATE_READY){
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
    }
}