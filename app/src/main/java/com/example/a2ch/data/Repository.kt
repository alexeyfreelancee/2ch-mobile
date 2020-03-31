package com.example.a2ch.data

import android.os.Build
import android.text.Html
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.category.CategoryBase
import com.example.a2ch.models.post.Post
import com.example.a2ch.util.getDate
import java.security.PublicKey


class Repository(private val retrofit: RetrofitClient) {

    suspend fun loadBoards(): BoardsBase {
        return retrofit.dvach.getBoards()
    }

    suspend fun loadCategory(name: String): CategoryBase {
        val category = retrofit.dvach.getCategory(name)
        val threads = category.threads
        category.threads.forEach {
            it.comment = stripHtml(it.comment)
            it.date = getDate(it.timestamp)
        }
        category.boardInfo = stripHtml(category.boardInfo)

        category.threads = threads.subList(0, (threads.size / 4))

        return category
    }

    suspend fun loadPosts(thread: String, board: String): List<Post> {
        val posts = retrofit.dvach.getThreadPosts(
            "get_thread", board, thread, 1
        )
        posts.forEach {
            it.date = getDate(it.timestamp)
            it.comment = stripHtml(it.comment)
        }

        return posts
    }

    suspend fun makePost(
        username: String,
        board: String,
        thread: String,
        comment: String,
        captchaAnswer: String, publicKey: String
    ): String {
        retrofit.dvach.checkCaptcha(publicKey, captchaAnswer)
        retrofit.dvach.makePost(
            1, "post",
            username, board, thread,
            "2chaptcha",
            publicKey, comment, captchaAnswer
        )
        return ""
    }

    suspend fun getCaptchaPublicKey(): String {
        return retrofit.dvach.getCaptchaPublicKey().id
    }

    private fun stripHtml(html: String?): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(html).toString()
        }
    }


}

