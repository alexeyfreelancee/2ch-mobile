package com.example.a2ch.models.post

import com.google.gson.annotations.SerializedName

data class FilePost(@SerializedName("tn_height")
                    val tnHeight: Int = 0,
                    @SerializedName("path")
                    val path: String = "",
                    @SerializedName("tn_width")
                    val tnWidth: Int = 0,
                    @SerializedName("thumbnail")
                    val thumbnail: String = "",
                    @SerializedName("size")
                    val size: Int = 0,
                    @SerializedName("name")
                    val name: String = "",
                    @SerializedName("width")
                    val width: Int = 0,
                    @SerializedName("type")
                    val type: Int = 0,
                    @SerializedName("height")
                    val height: Int = 0,
                    @SerializedName("md5")
                    val md: String = "")