package com.enty.musplayer.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enty.musplayer.data.other.Constants.UPDATE_PLAYER_DELAY
import com.enty.musplayer.exoplayer.MusicService
import com.enty.musplayer.exoplayer.MusicServiceConnection
import com.enty.musplayer.exoplayer.currentPlayingPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val playBackState = musicServiceConnection.playbackState

    private val _curDuration = MutableLiveData<Long>()
    val curDuration: LiveData<Long> = _curDuration

    private val _curPlayerPos = MutableLiveData<Long>()
    val curPlayerPos: LiveData<Long> = _curPlayerPos

    init {
        updateCurrentPlayerPos()
    }
    private fun updateCurrentPlayerPos(){
        viewModelScope.launch {
            while (true){
                val pos = playBackState.value?.currentPlayingPosition
                if (_curPlayerPos.value != pos){
                    _curPlayerPos.postValue(pos)
                    _curDuration.postValue(MusicService.songDuration)
                }
                delay(UPDATE_PLAYER_DELAY)
            }

        }
    }


}