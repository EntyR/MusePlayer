package com.enty.musplayer.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.enty.musplayer.data.enteties.Song
import com.enty.musplayer.data.other.Constants.COLLECTION_PATH
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SongDatabase {


    val fireBase = FirebaseFirestore.getInstance()
    val collection = fireBase.collection(COLLECTION_PATH)
    suspend fun getAllSongs(): List<Song>{
        try {
            val list = collection.get().await().toObjects(Song::class.java)
            Log.e("Firebase", "Song loaded from firebase")
            Log.e("Firebase", list[0].tittle)
            return list

        }catch (exeption: Exception){
            return emptyList()
        }
    }
}