package com.example.a2ch.models.boards

import com.google.gson.annotations.SerializedName

data class Category( @SerializedName("name")
                      val name: String = "",
                      @SerializedName("id")
                      val id: String = "")