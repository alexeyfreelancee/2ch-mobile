package com.alexey_vena.a2ch.ui.help_project

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexey_vena.a2ch.util.Event
import com.alexey_vena.a2ch.util.NO_INTERNET
import com.alexey_vena.a2ch.util.isNetworkAvailable


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

    private fun vibrate(context: Context){
        val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    150, VibrationEffect.EFFECT_CLICK
                )
            )
        } else {
            vibrator.vibrate(150)
        }
    }
    private fun addToClipboard(context: Context, text:String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Двачан", text)
        clipboard.setPrimaryClip(clip)
    }

    fun copyText(view: View, toast:String, copyText:String){
        vibrate(view.context)
        addToClipboard(view.context, copyText)
        textCopied.value = Event(toast)
    }
}

