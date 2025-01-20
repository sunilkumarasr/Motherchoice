package com.royalit.motherchoice

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

interface Observer {
    fun onUpdate(message: String)
}
object NotificationCenter {
    private val messageLiveData: MutableLiveData<String> = MutableLiveData()

    // Function to post a notification
    fun postNotification(message: String) {
        messageLiveData.value = message

    }

    // Function to observe notifications
    fun observe(owner: LifecycleOwner, observer: Observer) {
        messageLiveData.observe(owner) { message ->
            observer.onUpdate(message)
        }
    }
    fun clearData() {
        messageLiveData.value = "" // or messageLiveData.postValue(null)
    }
}