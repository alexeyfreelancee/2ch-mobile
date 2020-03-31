package com.example.a2ch.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

fun Fragment.parseString(id: Int): String {
    return  this.requireContext().resources.getString(id)
}

fun Activity.parseString(id: Int): String {
    return  this.applicationContext.resources.getString(id)
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun log(msg: String){
    Log.d("TAGG", msg)
}

fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
fun getDate(unix: Long) : String{
    val date = Date(unix * 1000)
    val sdf = SimpleDateFormat("dd.MM hh:mm", Locale.getDefault())
    return sdf.format(date)
}

fun provideCaptchaUrl(id: String) : String{
    return "https://2ch.hk/api/captcha/2chaptcha/image/$id"
}