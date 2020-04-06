package com.example.a2ch.models.threads

import com.google.gson.annotations.SerializedName

data class FilesItem(
                     @SerializedName("path")
                     val path: String = "")