package com.dvach_2ch.a2ch.models.threads

import com.google.gson.annotations.SerializedName

data class ThreadBase(
    @SerializedName("BoardName")
    val boardName: String = "",
    @SerializedName("BoardInfoOuter")
    val boardInfoOuter: String = "",
    @SerializedName("Board")
    val board: String = "",
    @SerializedName("BoardInfo")
    var boardInfo: String = "",
    @SerializedName("threads")
    val threadItems: List<ThreadItem>,
    @SerializedName("enable_names")
    val namesEnabled: Int = 0
)