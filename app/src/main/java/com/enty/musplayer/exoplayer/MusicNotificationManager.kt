package com.enty.musplayer.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.enty.musplayer.R
import com.enty.musplayer.data.other.Constants.NOTIFICATION_CHANEL_ID
import com.enty.musplayer.data.other.Constants.NOTIFICATION_ID

class MusicNotificationManager(
    val context: Context,
    val token: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    val newSongCallback: ()-> Unit
) {

    val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, token)
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANEL_ID,
            R.string.notification_chanel_name,
            R.string.notification_chanel_description,
            NOTIFICATION_ID,
            DescriptionAdapter(mediaController),
            notificationListener
        )
            .apply {
                setSmallIcon(R.drawable.ic_music)
                setMediaSessionToken(token)
            }

    }

    fun showNotificationManager(player: Player){
        notificationManager.setPlayer(player)
    }

    private  inner class  DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ): PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return  mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return  mediaController.metadata.description.description
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit

                })
            return  null
        }
    }
}