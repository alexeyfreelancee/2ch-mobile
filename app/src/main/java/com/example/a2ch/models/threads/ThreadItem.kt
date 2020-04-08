package com.example.a2ch.models.threads

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "thread_table")
data class ThreadItem(
    @PrimaryKey
    @SerializedName("thread_num")
    var threadNum: String = "",
    @SerializedName("posts")
    var posts: List<ThreadPost> = ArrayList(),
    @SerializedName("posts_count")
    var postsCount: Int = 0,
    var isFavourite: Boolean = false,
    var board: String = "",
    var timestamp: Long = 0

)