package com.example.a2ch.data

import android.os.Build
import android.text.Html
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaInfo
import com.example.a2ch.models.category.BoardInfo
import com.example.a2ch.models.post.MakePostError
import com.example.a2ch.models.post.Post
import com.example.a2ch.util.getDate


class Repository(private val retrofit: RetrofitClient) {

    suspend fun loadBoards(): BoardsBase {
        return retrofit.dvach.getBoards()
    }

    suspend fun loadThreads(name: String): BoardInfo {
        val threadsBase = retrofit.dvach.getThreads(name)

        threadsBase.threads.forEach {
            val thread = it.posts[0]
            thread.comment = stripHtml(thread.comment)
            thread.date = getDate(thread.timestamp)
        }

        threadsBase.boardInfo = stripHtml(threadsBase.boardInfo)
        return threadsBase
    }

    suspend fun loadPosts(thread: String, board: String): List<Post> {
        val posts = retrofit.dvach.getPosts(
            "get_thread", board, thread, 1
        )
        posts.forEach {
            it.date = getDate(it.timestamp)
            it.comment = stripHtml(it.comment)
            it.name = stripHtml(it.name)
            it.comment.replace(">>${it.parent}".toRegex(), "")
        }

        return posts
    }

    suspend fun makePostWithCaptcha(
        username: String,
        board: String,
        thread: String,
        comment: String,
        captchaAnswer: String,
        captchaId: String
    ): MakePostError {
        return retrofit.dvach.makePostWithCaptcha(
            1, "post",
            username, board, thread,
            "2chaptcha",
            captchaId, comment, captchaAnswer
        )
    }

    // не работает, тк всеми любимая мартышка не может сделать так,
    // чтобы сервер возвращал пасскод в формате json
    suspend fun makePostWithPasscode(
        username: String,
        board: String,
        thread: String,
        comment: String,
        usercode: String
    ): MakePostError {
        val responseUsercode = retrofit.dvach.getPasscode("auth", usercode)


        return retrofit.dvach.makePostWithPasscode(
            1, "post", username, board, thread, comment, responseUsercode
        )
    }

    suspend fun getCaptchaInfo(board: String, thread: String): CaptchaInfo {
        return retrofit.dvach.getCaptchaId(board, thread)
    }

    private fun stripHtml(html: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(html).toString()
        }
    }


    suspend fun getPost(board: String, postId:String) : Post{
        val posts = retrofit.dvach.getPost("get_post",board,postId)
        posts.forEach {
            it.date = getDate(it.timestamp)
            it.comment = stripHtml(it.comment)
            it.name = stripHtml(it.name)
            it.comment.replace(">>${it.parent}".toRegex(), "")
        }
        return posts[0]

    }

}

