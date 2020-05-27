package com.dvach_2ch.a2ch.util

import android.content.Context
import android.content.Context.MODE_PRIVATE

const val DARK_THEME = "DARK_THEME"


fun Context.checkDarkTheme(): Boolean{
    val prefs = this.getSharedPreferences("theme", MODE_PRIVATE)
    return prefs.getBoolean(DARK_THEME, false)
}

fun Context.setDarkTheme(){
    val prefs = this.getSharedPreferences("theme", MODE_PRIVATE)
    prefs.edit().putBoolean(DARK_THEME, true).apply()
}

fun Context.setDefaultTheme(){
    val prefs = this.getSharedPreferences("theme", MODE_PRIVATE)
    prefs.edit().putBoolean(DARK_THEME, false).apply()
}