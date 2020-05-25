package com.dvach_2ch.a2ch.data.networking

import com.dvach_2ch.a2ch.models.boards.BoardsBase
import com.dvach_2ch.a2ch.models.captcha.CaptchaData
import com.dvach_2ch.a2ch.models.threads.ThreadBase
import com.dvach_2ch.a2ch.models.util.MakePostResult

import com.dvach_2ch.a2ch.models.threads.ThreadPost
import retrofit2.http.*

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


    @GET("/api/captcha/recaptcha/id")
    suspend fun getCaptchaId(
        @Query("board") board: String,
        @Query("thread") thread: String
    ): CaptchaData



    @POST("makaba/posting.fcgi")
    suspend fun makePostWithCaptcha(
        @Query("json") json: Int = 1,
        @Query("task") task: String = "post",
        @Query("board") board: String,
        @Query("thread") thread: String,
        @Query("captcha_type") captchaType: String = "recaptcha",
        @Query("captcha-key") captchaKey: String,
        @Query("g-recaptcha-response") captchaResponse: String,
        @Query("comment", encoded = true) comment: String
    ) : MakePostResult


    @GET("makaba/mobile.fcgi")
    suspend fun getCurrentPost(
        @Query("task") task: String,
        @Query("board") board: String,
        @Query("post") post: String
    ) : List<ThreadPost>
}