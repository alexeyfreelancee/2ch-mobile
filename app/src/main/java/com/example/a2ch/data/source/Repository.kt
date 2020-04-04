package com.example.a2ch.data.source

import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaData
import com.example.a2ch.models.category.BoardInfo
import com.example.a2ch.models.category.Thread
import com.example.a2ch.models.post.MakePostResult
import com.example.a2ch.models.post.Post
import com.example.a2ch.util.getDate
import com.example.a2ch.util.log
import com.example.a2ch.util.parseDigits


class Repository(private val retrofit: RetrofitClient) {

    suspend fun loadBoards(): BoardsBase {
        return retrofit.dvach.getBoards()
    }

    suspend fun loadThreads(name: String): BoardInfo {
        val threadsBase = retrofit.dvach.getThreads(name)

        threadsBase.threads.forEach {
            val thread = it.posts[0]
            thread.date = getDate(thread.timestamp)
        }
        return threadsBase
    }


    suspend fun makePostWithCaptcha(
        username: String,
        board: String,
        thread: String,
        comment: String,
        captchaAnswer: String,
        captchaId: String
    ): Boolean {
        val result = retrofit.dvach.makePostWithCaptcha(
            1, "post",
            username, board, thread,
            "2chaptcha",
            captchaId, comment, captchaAnswer
        )
        return result.error == null
    }

    // не работает, тк всеми любимая мартышка не может сделать так,
    // чтобы сервер возвращал пасскод в формате json
    suspend fun makePostWithPasscode(
        username: String,
        board: String,
        thread: String,
        comment: String,
        usercode: String
    ): MakePostResult {
        val responseUsercode = retrofit.dvach.getPasscode("auth", usercode)
        return retrofit.dvach.makePostWithPasscode(
            1, "post", username, board, thread, comment, responseUsercode
        )
    }

    suspend fun getCaptchaData(board: String, thread: String): CaptchaData {
        return retrofit.dvach.getCaptchaId(board, thread)
    }


    suspend fun loadPosts(thread: String, board: String): List<Post> {
        val posts = retrofit.dvach.getPosts(
            "get_thread", board, thread, 1
        )
        preparePosts(posts)
        return posts
    }


    suspend fun getPost(href: String): Post {
        //href example /b/res/216879164.html#216879164\
        val boardId = href.split("/")
        val postId = href
            .substring(href.lastIndexOf("#") + 1)
            .parseDigits()

        val posts = retrofit.dvach.getCurrentPost("get_post", boardId[1], postId)

        preparePosts(posts)
        return posts[0]
    }

    suspend fun addToHistory(board: String, thread: String){
        //TODO dao shit
    }

    suspend fun addToFavourites(board: String, thread: String) : Boolean{
        //TODO dao shit
        return true
    }

    suspend fun loadFavourites(): List<Thread>{
        //TODO dao shit
        return emptyList()
    }

    suspend fun loadHistory(): List<Thread>{
        //TODO dao shit
        return emptyList()
    }
    private fun preparePosts(posts: List<Post>) {
        posts.forEach {
            it.date = getDate(it.timestamp)
        }
    }

}

