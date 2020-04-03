package com.example.a2ch.models.category

import android.text.Spanned
import com.google.gson.annotations.SerializedName

data class BoardInfo(@SerializedName("BoardName")
                        val boardName: String = "",
                     @SerializedName("BoardInfoOuter")
                        val boardInfoOuter: String = "",
                     @SerializedName("Board")
                        val board: String = "",
                     @SerializedName("BoardInfo")
                        var boardInfo: String = "",
                     @SerializedName("threads")
                        val threads: List<ThreadBase>)