package com.alexey_vena.a2ch.models.boards

import com.google.gson.annotations.SerializedName

data class Board(@SerializedName("name")
                      val name: String = "",
                 @SerializedName("id")
                      val id: String = "",
                 val isHeader: Boolean = false)