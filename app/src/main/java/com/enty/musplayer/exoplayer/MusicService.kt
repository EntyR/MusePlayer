package com.enty.musplayer.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.enty.musplayer.data.other.Constants.MEDIA_ROOT_ID
import com.enty.musplayer.data.other.Constants.NETWORK_ERROR
import com.enty.musplayer.exoplayer.callbacks.MusicPlaybackPreparer
import com.enty.musplayer.exoplayer.callbacks.MusicPlayerEventListener
import com.enty.musplayer.exoplayer.callbacks.MusicPlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat(){

    var isForegroundService = false

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer
    @Inject
    lateinit var firebaseMusicStore: FirebaseMusicStore

    private lateinit var musicPlayerListener: MusicPlayerEventListener
    var isPlayerInitialized = false
    lateinit var notificationManager: MusicNotificationManager
    val job = Job()
    val serviceScoped = CoroutineScope(Dispatchers.Main + job)

    lateinit var mediaSession: MediaSessionCompat
    lateinit var mediaSessionConnector: MediaSessionConnector

    private var currentPlayingSong: MediaMetadataCompat? = null

    companion object{
        var songDuration = 0L
    }


    override fun onCreate() {
        super.onCreate()

        serviceScoped.launch {
            Log.e("MusicService", "fetchMediaData")
            firebaseMusicStore.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        notificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerListener(this)){
            songDuration = exoPlayer.duration
        }

        val musicPlayBackPreparer = MusicPlaybackPreparer(firebaseMusicStore){
            currentPlayingSong = it
            preparePlayer(
                firebaseMusicStore.songs,
                it,
                true
            )
        }



        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlayBackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerListener)

        notificationManager.showNotificationManager(exoPlayer)
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return  firebaseMusicStore.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicStore.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScoped.cancel()

        exoPlayer.removeListener(musicPlayerListener)
        exoPlayer.release()
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.e("MusicService", "onGetRoot")
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        Log.e("MusicService", "LoadChildren")
        when (parentId){
            MEDIA_ROOT_ID -> {

                val resSend = firebaseMusicStore.whenReady {
                    if (it){
                        result.sendResult(firebaseMusicStore.asMediaItem())
                        if (!isPlayerInitialized && firebaseMusicStore.songs.isNotEmpty()){
                            preparePlayer(firebaseMusicStore.songs, firebaseMusicStore.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else{
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if(!resSend){
                    result.detach()
                }
        }
        }

    }
}