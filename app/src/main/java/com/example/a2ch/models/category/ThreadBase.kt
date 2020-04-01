package com.example.a2ch.models.category

import com.google.gson.annotations.SerializedName

data class ThreadBase(@SerializedName("thread_num")
                      val threadNum: String = "",
                      @SerializedName("files_count")
                      val filesCount: Int = 0,
                      @SerializedName("posts")
                      val posts: List<Thread>,
                      @SerializedName("posts_count")
                      val postsCount: Int = 0)