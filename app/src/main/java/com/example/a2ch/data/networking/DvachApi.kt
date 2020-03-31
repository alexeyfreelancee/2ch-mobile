package com.example.a2ch.data.networking

import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaId
import com.example.a2ch.models.category.CategoryBase
import com.example.a2ch.models.post.Post
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DvachApi {

    @GET("makaba/mobile.fcgi?task=get_boards")
    suspend fun getBoards(): BoardsBase

    @GET("{name}/catalog_num.json")
    suspend fun getCategory(
        @Path("name") name: String
    ): CategoryBase

    @GET("makaba/mobile.fcgi")
    suspend fun getThreadPosts(
        @Query("task") task: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("post") post: Int
    ): List<Post>


    @GET("/api/captcha/2chaptcha/service_id")
    suspend fun getCaptchaPublicKey(): CaptchaId

    @GET("/api/captcha/2chaptcha/check/{id}")
    suspend fun checkCaptcha(
        @Path("id") id: String,
        @Query("value") captchaAnswer: String
    )

    @POST("makaba/posting.fcgi")
    suspend fun makePost(
        @Query("json") json: Int,
        @Query("task") task: String,
        @Query("name") username: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("captcha_type") captchaType: String,
        @Query("captcha-key") captchaId: String,
        @Query("comment") comment: String,
        @Query("g-recaptcha-response") captchaResponse: String
    )

}