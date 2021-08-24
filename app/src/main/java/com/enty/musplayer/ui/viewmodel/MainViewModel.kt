package com.enty.musplayer.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enty.musplayer.data.enteties.Song
import com.enty.musplayer.data.other.Constants.MEDIA_ROOT_ID
import com.enty.musplayer.data.other.Resource
import com.enty.musplayer.exoplayer.MusicServiceConnection
import com.enty.musplayer.exoplayer.isPlayEnabled
import com.enty.musplayer.exoplayer.isPlaying
import com.enty.musplayer.exoplayer.isPrepared

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {


    private val _songItem = MutableLiveData<Resource<List<Song>>>()
    val songItem: LiveData<Resource<List<Song>>> = _songItem

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _songItem.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {

                Log.e("View Model", "onChildrenLoaded" )
                super.onChildrenLoaded(parentId, children)
                val songs = children.map {
                    Song(
                        it.description.mediaId.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString(),
                        it.description.title.toString(),
                        it.description.subtitle.toString()
                    )
                }
                _songItem.postValue(Resource.success(songs))
            }
        })

    }
    fun skipToNext(){
        musicServiceConnection.transportControls.skipToNext()
    }
    fun skipToPrevious(){
        musicServiceConnection.transportControls.skipToPrevious()
    }
    fun seekTo(pos: Long){
        musicServiceConnection.transportControls.seekTo(pos)

    }

    fun playOrToggle(
        mediaItem: Song,
        toggle: Boolean){

        val isPrepered = playbackState.value?.isPrepared?: false
        if (isPrepered && mediaItem.songID == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){

            playbackState.value?.let {
                when {
                    it.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    it.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }
        else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.songID, null)

        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){})
    }


}














