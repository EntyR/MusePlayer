package com.enty.musplayer.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.enty.musplayer.R
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdapter@Inject constructor(
    private val glide: RequestManager
): BaseSongAdapter(R.layout.list_item){

    override var differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseSongAdapter.SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.tittle
            tvSecondary.text = song.description
            glide.load(song).into(ivItemImage)
            setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }


}