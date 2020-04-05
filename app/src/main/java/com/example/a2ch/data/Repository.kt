package com.example.a2ch.data

import com.example.a2ch.data.db.AppDatabase
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaData
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadItem
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.MakePostResult
import com.example.a2ch.util.getDate
import com.example.a2ch.util.isNetworkAvailable
import com.example.a2ch.util.log
import com.example.a2ch.util.parseDigits


class Repository(private val retrofit: RetrofitClient, private val db: AppDatabase) {

    suspend fun loadBoards(): BoardsBase {
        return retrofit.dvach.getBoards()
    }

    suspend fun loadBoardInfo(board: String): ThreadBase {
        val threadsBase = retrofit.dvach.getThreads(board)

        threadsBase.threadItems.forEach {
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

    suspend fun isFavourite(board: String, threadNum: String): Boolean{
        val thread = getThread(board, threadNum)
        return thread?.isFavourite ?: false

    }
    suspend fun loadPosts(thread: String, board: String): List<ThreadPost> {
        return if (isNetworkAvailable()) {
            val posts = retrofit.dvach.getPosts(
                "get_thread", board, thread, 1
            )
            addToDatabase(board, thread)
            preparePosts(posts)
        } else {
            db.threadDao().getThread(board, thread)!!.posts
        }

    }

    suspend fun getPost(href: String): ThreadPost {
        //href example /b/res/216879164.html#216879164\
        val boardId = href.split("/")
        val postId = href
            .substring(href.lastIndexOf("#") + 1)
            .parseDigits()

        val currentPost = retrofit.dvach.getCurrentPost("get_post", boardId[1], postId)
        return preparePosts(currentPost)[0]
    }

    private suspend fun addToDatabase(board: String, threadNum: String) {
        val thread = getThread(board, threadNum)
        val posts = retrofit.dvach.getPosts(
            "get_thread", board, threadNum, 1
        )
        if(thread!=null){
            posts.forEach {post->
                post.postsCount = posts.size
                post.board = board
            }
            thread.board = board
            thread.posts = posts
            db.threadDao().insertWithTimestamp(thread)
        }

    }

    suspend fun getThread(board: String, threadNum: String): ThreadItem? {
        val thread = db.threadDao().getThread(board, threadNum)
        return thread ?: loadBoardInfo(board).threadItems.find { it.threadNum == threadNum }
    }

    suspend fun addToFavourites(board: String, threadItem: ThreadItem) {
        threadItem.board = board

        threadItem.posts[0].board = board
        threadItem.isFavourite = true
        db.threadDao().insertWithTimestamp(threadItem)
    }

    suspend fun removeFromFavourites(threadPost: ThreadPost) {
        val threadItem = getThread(threadPost.board, threadPost.num)

        threadItem?.let {
            threadItem.isFavourite = false
            db.threadDao().insertWithTimestamp(threadItem)
        }

    }

    suspend fun removeFromFavourites(threadItem: ThreadItem) {
        threadItem.isFavourite = false
        db.threadDao().insertWithTimestamp(threadItem)
    }

    suspend fun loadFavourites(): List<ThreadPost> {
        val threads = db.threadDao().getFavouriteThreads()
        val result = ArrayList<ThreadPost>()
        threads.forEach {
            result.add(it.posts[0])
        }
        return result
    }

    suspend fun loadHistory(): List<ThreadPost> {
        val threads = db.threadDao().getHistoryThreads()
        val result = ArrayList<ThreadPost>()
        threads.forEach {
            result.add(it.posts[0])
        }
        return result
    }

    private fun preparePosts(posts: List<ThreadPost>): List<ThreadPost> {
        posts.forEach {
            it.date = getDate(it.timestamp)
        }
        return posts
    }

}

