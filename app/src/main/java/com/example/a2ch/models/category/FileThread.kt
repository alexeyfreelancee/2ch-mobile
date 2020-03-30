package com.example.a2ch.models.category

import com.google.gson.annotations.SerializedName

data class FileThread(@SerializedName("tn_height")
                     val tnHeight: Long = 0,
                      @SerializedName("thumbnail")
                     val thumbnail: String = "",
                      @SerializedName("nsfw")
                     val nsfw: Long = 0,
                      @SerializedName("type")
                     val type: Long = 0,
                      @SerializedName("path")
                     val path: String = "",
                      @SerializedName("tn_width")
                     val tnWidth: Long = 0,
                      @SerializedName("size")
                     val size: Long = 0,
                      @SerializedName("displayname")
                     val displayname: String = "",
                      @SerializedName("name")
                     val name: String = "",
                      @SerializedName("width")
                     val width: Long = 0,
                      @SerializedName("fullname")
                     val fullname: String = "",
                      @SerializedName("height")
                     val height: Long = 0,
                      @SerializedName("md5")
                     val md: String = "")