package com.enty.musplayer.frgments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.enty.musplayer.R
import com.enty.musplayer.data.enteties.Song
import com.enty.musplayer.data.other.Status.SUCCESS
import com.enty.musplayer.exoplayer.isPlaying
import com.enty.musplayer.exoplayer.toSong
import com.enty.musplayer.ui.viewmodel.MainViewModel
import com.enty.musplayer.ui.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment: Fragment(R.layout.fragment_song) {

    private var shouldUpdateSeekBar: Boolean = true

    @Inject
    lateinit var glide: RequestManager

    lateinit var mainViewModel: MainViewModel
    val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null
    private var playBackStateCompat: PlaybackStateCompat? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObserver()

        ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggle(it, true)
            }
        }
        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPrevious()
        }
        ivSkip.setOnClickListener {
            mainViewModel.skipToNext()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })
    }

    private fun updateTittleAndSongImage(song: Song){
        val tittle = "${song.tittle} - ${song.description}"
        tvSongName.text = tittle
        glide.load(song.iconUrl).into(ivSongImage)
    }
    private fun subscribeToObserver(){
        mainViewModel.songItem.observe(viewLifecycleOwner){
            when(it.status){
                SUCCESS -> {
                    it.data?.let { songs ->
                        if (curPlayingSong ==null && songs.isNotEmpty()){
                            curPlayingSong = songs[0]
                            updateTittleAndSongImage(songs[0])
                        }
                    }
                }
                else -> Unit
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            updateTittleAndSongImage(curPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playBackStateCompat = it
            ivPlayPauseDetail.setImageResource(
                if (playBackStateCompat?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPos.observe(viewLifecycleOwner){
            if (shouldUpdateSeekBar){
                seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }
        songViewModel.curDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }

    }

    private fun setCurrentPlayerTimeToTextView(ms:Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }

}
















