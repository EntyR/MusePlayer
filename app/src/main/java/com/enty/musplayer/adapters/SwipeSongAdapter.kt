package com.enty.musplayer.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.enty.musplayer.R
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeSongAdapter: BaseSongAdapter(R.layout.swipe_item){

    override var differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseSongAdapter.SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            val text = "${song.tittle} - ${song.description}"
            tvPrimary.text = text

            setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }


}