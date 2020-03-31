package com.example.a2ch.data.networking

import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaInfo
import com.example.a2ch.models.category.CategoryBase
import com.example.a2ch.models.post.MakePostError
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


    @GET("/api/captcha/2chaptcha/id")
    suspend fun getCaptchaId(
        @Query("board") board: String,
        @Query("thread") thread: String
    ): CaptchaInfo

    @GET("/api/captcha/2chaptcha/check/{id}")
    suspend fun checkCaptcha(
        @Path("id") id: String,
        @Query("value") captchaAnswer: String
    )

    @POST("makaba/posting.fcgi")
    suspend fun makePostWithCaptcha(
        @Query("json") json: Int,
        @Query("task") task: String,
        @Query("name") username: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("captcha_type") captchaType: String,
        @Query("2chaptcha_id") captchaId: String,
        @Query("comment") comment: String,
        @Query("2chaptcha_value") captchaValue: String
    ) : MakePostError

    @POST("makaba/posting.fcgi")
    suspend fun makePostWithPasscode(
        @Query("json") json: Int,
        @Query("task") task: String,
        @Query("name") username: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("comment") comment: String,
        @Query("usercode") usercode: String
    ) : MakePostError


    @POST("makaba/makaba.fcgi")
    suspend fun getPasscode(
        @Query("task") task:String,
        @Query("usercode") usercode: String
    ) : String

}