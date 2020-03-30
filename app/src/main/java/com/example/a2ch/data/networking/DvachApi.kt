package com.example.a2ch.data.networking

import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.category.CategoryBase
import com.example.a2ch.models.post.Post
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DvachApi {

    @GET("makaba/mobile.fcgi?task=get_boards")
    suspend fun getBoards() : BoardsBase

    @GET("{name}/catalog_num.json")
    suspend fun getCategory(
        @Path("name") name:String
    ) : CategoryBase

    @GET("makaba/mobile.fcgi")
    suspend fun getThreadPosts(
        @Query("task") task: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("post") post: Int) : List<Post>
}