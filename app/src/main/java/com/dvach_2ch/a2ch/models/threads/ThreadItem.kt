package com.dvach_2ch.a2ch.models.threads

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "thread_table")
data class ThreadItem(
    @PrimaryKey
    @SerializedName("num")
    var num: String = "",
    @SerializedName("posts")
    var posts: List<ThreadPost> = ArrayList(),
    @SerializedName("files")
    val files: List<FilesItem> = ArrayList(),
    @SerializedName("posts_count")
    var postsCount: Int = 0,
    @SerializedName("comment")
    var comment: String = "",
    @SerializedName("name")
    var name:String = "",
    @SerializedName("timestamp")
    var loadTime: Long = 0,
    @SerializedName("subject")
    var subject:String = "",
    var isFavourite: Boolean = false,
    var board: String = "",
    var saveTime: Long = 0



)