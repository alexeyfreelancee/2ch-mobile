package com.example.a2ch.data.networking

import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.category.CategoryBase
import retrofit2.http.GET
import retrofit2.http.Path

interface DvachApi {

    @GET("makaba/mobile.fcgi?task=get_boards")
    suspend fun getBoards() : BoardsBase

    @GET("{name}/catalog_num.json")
    suspend fun getCategory(
        @Path("name") name:String
    ) : CategoryBase
}