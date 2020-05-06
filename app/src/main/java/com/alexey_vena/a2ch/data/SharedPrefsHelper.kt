package com.alexey_vena.a2ch.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.alexey_vena.a2ch.util.USERNAME

class SharedPrefsHelper (private val context:Context){
    private val prefs = context.getSharedPreferences("prefs", MODE_PRIVATE)

    fun loadUsername() : String?{
        return prefs.getString(USERNAME, "")
    }

    fun saveUsername(username:String){
        prefs.edit().putString(USERNAME, username).apply()
    }

}