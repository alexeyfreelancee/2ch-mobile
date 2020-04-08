package com.example.a2ch.models.threads

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class ThreadPost(
    @SerializedName("subject")
    val subject: String = "",
    @SerializedName("num")
    val num: String = "",
    @SerializedName("name")
    var name: String = "",
    @SerializedName("files")
    val files: List<FilesItem> = ArrayList(),
    @SerializedName("comment")
    var comment: String = "",
    @SerializedName("posts_count")
    var postsCount: Int = 0,
    @SerializedName("timestamp")
    val timestamp: Long = 0,
    var date: String = "",
    var board: String = "",
    var isDate: Boolean = false,
    var isRead: Boolean = false
)