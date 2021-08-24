package com.enty.musplayer.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.enty.musplayer.data.remote.SongDatabase
import com.enty.musplayer.exoplayer.FirebaseMusicStore.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicStore @Inject constructor(val songDatabase: SongDatabase) {

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = STATE_INITIALIZING
        val allSongs = songDatabase.getAllSongs()
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.description)
                .putString(METADATA_KEY_TITLE, song.tittle)
                .putString(METADATA_KEY_MEDIA_ID, song.songID)
                .putString(METADATA_KEY_ALBUM, song.description)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.iconUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.musicUrl)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.tittle)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.iconUrl)
                .build()
        }
        state = STATE_INITIALIZED
    }
     fun asMediaSource(defaultMediaSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource{
         val concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
         songs.map { song ->
             val mediaSource = ProgressiveMediaSource.Factory(defaultMediaSourceFactory)
                 .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
             concatenatingMediaSource.addMediaSource(mediaSource)

         }
         return concatenatingMediaSource
     }

    fun asMediaItem() = songs.map {  song ->
        val desc = MediaDescriptionCompat.Builder()
            .setIconUri(song.description.iconUri)
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setMediaId(song.description.mediaId)
            .setTitle(song.description.title)
            .setDescription(song.description.description)
            .setSubtitle(song.description.description)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR){
                synchronized(onReadyListener){
                    field = value
                    onReadyListener.forEach {
                        it(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }

        }

    fun whenReady(action: (Boolean) -> Unit): Boolean{
        if (state ==  STATE_CREATED ||  state == STATE_INITIALIZING) {
            onReadyListener += action
            return false
        } else{
            action(state == STATE_INITIALIZED)
            return true
        }
    }

    enum class State{
        STATE_CREATED,
        STATE_INITIALIZING,
        STATE_INITIALIZED,
        STATE_ERROR
    }
}