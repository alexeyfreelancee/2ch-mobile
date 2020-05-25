package com.dvach_2ch.a2ch.ui.help_project

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dvach_2ch.a2ch.util.Event
import com.dvach_2ch.a2ch.util.NO_INTERNET
import com.dvach_2ch.a2ch.util.isNetworkAvailable


class HelpProjectViewModel() : ViewModel() {
    val openAd = MutableLiveData<Event<String>>()
    val error = MutableLiveData<Event<String>>()
    val textCopied = MutableLiveData<Event<String>>()


    fun watchAd(){
        if(isNetworkAvailable()){
            openAd.value = Event("Hello world")
        } else{
            error.value = Event(NO_INTERNET)
        }

    }


    private fun addToClipboard(context: Context, text:String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Двачан", text)
        clipboard.setPrimaryClip(clip)
    }

    fun copyText(view: View, toast:String, copyText:String){
        addToClipboard(view.context, copyText)
        textCopied.value = Event(toast)
    }
}

