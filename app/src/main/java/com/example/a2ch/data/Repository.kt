package com.example.a2ch.data

import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.category.CategoryBase
import retrofit2.Retrofit

class Repository(private val retrofit: RetrofitClient) {

    suspend fun loadBoards() : BoardsBase{
        return retrofit.dvach.getBoards()
    }

    suspend fun loadCategory(name: String) : CategoryBase{
        return retrofit.dvach.getCategory(name)
    }
}