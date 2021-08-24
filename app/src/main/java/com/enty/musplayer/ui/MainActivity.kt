package com.enty.musplayer.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.enty.musplayer.R
import com.enty.musplayer.adapters.SwipeSongAdapter
import com.enty.musplayer.data.enteties.Song
import com.enty.musplayer.data.other.Status.*
import com.enty.musplayer.exoplayer.isPlaying
import com.enty.musplayer.exoplayer.toSong
import com.enty.musplayer.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide: RequestManager

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var playBackState: PlaybackStateCompat? = null

    private var currentlyPlayingSong: Song? = null
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter
        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playBackState?.isPlaying == true){
                    viewModel.playOrToggle(swipeSongAdapter.songs[position], false)
                }
                else {
                    currentlyPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })
        ivPlayPause.setOnClickListener{
            currentlyPlayingSong?.let {
                viewModel.playOrToggle(it, true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(R.id.globalActionToSongFragment)
        }

        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.homeFragments -> showBottomBar()
                R.id.songFragment -> hideBottomBar()

            }
        }

    }

    private fun hideBottomBar(){
        ivCurSongImage.isVisible = false
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun showBottomBar(){
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }


    private fun switchToSong(song:Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1){
            vpSong.currentItem = newItemIndex
            currentlyPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        viewModel.songItem.observe(this){
            it?.let{ result ->
                when(result.status){
                    SUCCESS -> {
                        result.data?.let {
                            swipeSongAdapter.songs = it
                            if (it.isNotEmpty()){
                                glide.load((currentlyPlayingSong?: it[0]).iconUrl).into(ivCurSongImage)
                            }
                            switchToSong(currentlyPlayingSong?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }
        viewModel.curPlayingSong.observe(this){
            if (it == null) return@observe

            currentlyPlayingSong = it.toSong()
            glide.load(currentlyPlayingSong?.iconUrl).into(ivCurSongImage)
            switchToSong(currentlyPlayingSong?: return@observe)
        }
        viewModel.playbackState.observe(this){
             playBackState = it

            ivPlayPause.setImageResource(
                if (playBackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        viewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let {
                when(it.status){
                    ERROR -> {Snackbar.make(rootLayout, it.message?: "An unknown error occured", Snackbar.LENGTH_LONG)}
                    else -> Unit
                }
            }
        }
        viewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let {
                when(it.status){
                    ERROR -> {Snackbar.make(rootLayout, it.message?: "An unknown error occured", Snackbar.LENGTH_LONG)}
                    else -> Unit
                }
            }
        }
    }
}