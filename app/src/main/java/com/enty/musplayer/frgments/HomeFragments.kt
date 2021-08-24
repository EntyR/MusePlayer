package com.enty.musplayer.frgments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.enty.musplayer.R
import com.enty.musplayer.adapters.SongAdapter
import com.enty.musplayer.data.other.Status
import com.enty.musplayer.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject


@AndroidEntryPoint
class  HomeFragments: Fragment(R.layout.fragment_home) {
    lateinit var  mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        subscribeToObservers()
        songAdapter.setItemClickListener {
            mainViewModel.playOrToggle(it, false)
        }
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())

    }

    private fun subscribeToObservers(){
        mainViewModel.songItem.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {

                    allSongsProgressBar.isVisible = false
                    it.data?.let {
                        songAdapter.songs = it
                    }

                }
                Status.ERROR-> Unit
                Status.LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }

}