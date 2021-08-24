package com.enty.musplayer.exoplayer

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPrepared
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING||
            state == PlaybackStateCompat.STATE_PAUSED
inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_BUFFERING

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L||
            (actions and  PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                    state == PlaybackStateCompat.STATE_PAUSED)
inline val PlaybackStateCompat.currentPlayingPosition: Long
    get() = if(state == STATE_PLAYING) {
        val deltaTime = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (deltaTime * playbackSpeed)).toLong()

    }
            else {position}
