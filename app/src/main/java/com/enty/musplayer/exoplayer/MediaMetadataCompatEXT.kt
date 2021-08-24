package com.enty.musplayer.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.enty.musplayer.data.enteties.Song

fun MediaMetadataCompat.toSong(): Song?{
    return description?.let {
        Song(
            it.mediaId?: "",
            it.mediaUri.toString(),
            it.iconUri.toString(),
            it.title.toString(),
            it.description.toString()
        )
    }
}
