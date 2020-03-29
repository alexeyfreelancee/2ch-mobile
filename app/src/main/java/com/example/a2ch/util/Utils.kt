package com.example.a2ch.util

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment

fun Fragment.parseString(id: Int): String {
    return  this.requireContext().resources.getString(id)
}

fun Activity.parseString(id: Int): String {
    return  this.applicationContext.resources.getString(id)
}