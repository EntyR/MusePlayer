package com.enty.musplayer.data.other

open class Event<out T>(val data:T) {
    var hasBeenHandled: Boolean = false
        private set

    fun getContentIfNotHandled(): T?{
        return if(hasBeenHandled){
            null
        }
        else{
            hasBeenHandled = true
            data
        }
    }
    fun peekContent() = data
}