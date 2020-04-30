package com.example.a2ch.data.networking

import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaData
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.util.MakePostResult

import com.example.a2ch.models.threads.ThreadPost
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DvachApi {

    @GET("makaba/mobile.fcgi?task=get_boards")
    suspend fun getBoards(): BoardsBase

    @GET("{board}/catalog_num.json")
    suspend fun getThreads(
        @Path("board") board: String
    ): ThreadBase



    //http(s)://2ch.hk/makaba/mobile.fcgi?task=get_thread&board=abu&thread=39220&post=252
    @GET("makaba/mobile.fcgi")
    suspend fun getPosts(
        @Query("task") task: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("post") post: Int
    ): List<ThreadPost>


    @GET("/api/captcha/2chaptcha/id")
    suspend fun getCaptchaId(
        @Query("board") board: String,
        @Query("thread") thread: String
    ): CaptchaData

    //https://2ch.hk/api/captcha/invisible_recaptcha/id
    @GET("/api/captcha/invisible_recaptcha/id")
    suspend fun getInvisibleCaptchaId(): CaptchaData

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
    ) : MakePostResult

    @POST("makaba/posting.fcgi")
    suspend fun makePostWithPasscode(
        @Query("json") json: Int,
        @Query("task") task: String,
        @Query("name") username: String,
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("comment") comment: String,
        @Query("usercode") usercode: String
    ) : MakePostResult


    @POST("makaba/makaba.fcgi")
    suspend fun getPasscode(
        @Query("task") task:String,
        @Query("usercode") usercode: String
    ) : String


    @GET("makaba/mobile.fcgi")
    suspend fun getCurrentPost(
        @Query("task") task: String,
        @Query("board") board: String,
        @Query("post") post: String
    ) : List<ThreadPost>
}