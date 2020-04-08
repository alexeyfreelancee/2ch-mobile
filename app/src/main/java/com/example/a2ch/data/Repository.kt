package com.example.a2ch.data

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.example.a2ch.data.db.AppDatabase
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaData
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadItem
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.MakePostResult
import com.example.a2ch.util.isNetworkAvailable
import com.example.a2ch.util.parseDigits
import com.example.a2ch.util.parseThreadDate
import kotlinx.coroutines.*
import java.io.File


class Repository(private val retrofit: RetrofitClient, private val db: AppDatabase) {


    suspend fun loadBoards(): BoardsBase {
        val result = CoroutineScope(Dispatchers.IO).async {
            retrofit.dvach.getBoards()
        }
        return result.await()
    }

    suspend fun loadBoardInfo(board: String): ThreadBase {
        val result = CoroutineScope(Dispatchers.IO).async {
            retrofit.dvach.getThreads(board)
        }
        return result.await()
    }


    suspend fun makePostWithCaptcha(
        username: String,
        board: String,
        thread: String,
        comment: String,
        captchaAnswer: String,
        captchaId: String
    ): Boolean {
        val result = CoroutineScope(Dispatchers.IO).async {
            retrofit.dvach.makePostWithCaptcha(
                1, "post",
                username, board, thread,
                "2chaptcha",
                captchaId, comment, captchaAnswer
            )
        }

        return result.await().error == null
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

    suspend fun isFavourite(board: String, threadNum: String): Boolean {
        val thread = CoroutineScope(Dispatchers.IO).async {
            getThread(board, threadNum)
        }
        return thread.await()?.isFavourite ?: false

    }

    suspend fun loadPosts(thread: String, board: String): List<ThreadPost> {
        val result = CoroutineScope(Dispatchers.IO).async {
            if (isNetworkAvailable()) {
                addToDatabase(board, thread)
            }
            db.threadDao().getThread(board, thread)
        }


        return result.await()?.posts ?: getThread(board, thread)!!.posts

    }

    suspend fun getPost(href: String, threadNum: String): ThreadPost {
        //href example /b/res/216879164.html#216879164\

        val boardId = href.split("/")
        val postId = href
            .substring(href.lastIndexOf("#") + 1)
            .parseDigits()

        val dbPost = CoroutineScope(Dispatchers.IO).async {
            val dbThread = db.threadDao().getThread(boardId[1], threadNum)
            dbThread?.posts?.find { post ->
                post.num == postId
            }

        }
        var networkPost: Deferred<ThreadPost?>? = null

        if (isNetworkAvailable() && dbPost.await() == null) {
            networkPost = CoroutineScope(Dispatchers.IO).async {
                retrofit.dvach.getCurrentPost("get_post", boardId[1], postId)[0]
            }
        }



        return dbPost.await() ?: networkPost!!.await()!!
    }

    private suspend fun addToDatabase(board: String, threadNum: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val thread = getThread(board, threadNum)
            val posts = retrofit.dvach.getPosts(
                "get_thread", board, threadNum, 1
            )
            if (thread != null) {
                val needToUpdatePosts = thread.posts.size == posts.size

                posts.forEach { post ->
                    post.postsCount = posts.size
                    post.board = board
                }

                thread.board = board
                if (needToUpdatePosts) {
                    //Важно сохранять кол-во прочитанных постов
                    for ((index, post) in thread.posts.withIndex()) {
                        posts[index].isRead = post.isRead
                    }
                }
                thread.posts = posts
                db.threadDao().saveWithTimestamp(thread)
            }
        }
    }

    suspend fun getThread(board: String, threadNum: String): ThreadItem? {
        val thread = CoroutineScope(Dispatchers.IO).async {
            db.threadDao().getThread(board, threadNum)
        }

        return thread.await() ?: loadBoardInfo(board).threadItems.find { it.threadNum == threadNum }
    }

    suspend fun addToFavourites(board: String, threadItem: ThreadItem) {
        CoroutineScope(Dispatchers.IO).launch {
            threadItem.board = board

            threadItem.posts[0].board = board
            threadItem.isFavourite = true
            db.threadDao().saveWithTimestamp(threadItem)
        }
    }

    suspend fun removeFromFavourites(threadPost: ThreadPost) {

        val threadItem = getThread(threadPost.board, threadPost.num)

        threadItem?.let {
            threadItem.isFavourite = false
            db.threadDao().saveWithTimestamp(threadItem)
        }

    }

    suspend fun readPost(board: String, threadNum: String, position: Int) {
        try {

            val thread = db.threadDao().getThread(board, threadNum)

            if (thread != null) {
                if (!thread.posts[position].isRead) {
                    if (position != 0) thread.posts[position - 1].isRead = true
                    thread.posts[position].isRead = true
                    if (position != thread.posts.size - 1) thread.posts[position + 1].isRead = true
                    db.threadDao().saveThread(thread)
                }
            }

        } catch (ex: Exception) {
        }

    }

    suspend fun computeUnreadPosts(threadNum: String, board: String): Int? {
        var postsWereRead = 0
        db.threadDao().getThread(board, threadNum)?.posts?.forEach {
            if (it.isRead) postsWereRead++
        }

        val postsSaved = db.threadDao().getThread(board, threadNum)?.posts?.size ?: 0
        val unreadPosts = postsSaved - postsWereRead


        return if (postsSaved == 0 || unreadPosts == 0 || postsWereRead == 0) {
            null
        } else {
            unreadPosts
        }
    }


    suspend fun removeFromFavourites(threadItem: ThreadItem) {
        threadItem.isFavourite = false
        db.threadDao().saveWithTimestamp(threadItem)
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
        val dates = HashSet<String>()
        val threads = db.threadDao().getHistoryThreads()
        val result = ArrayList<ThreadPost>()


        threads.forEach {
            val date = parseThreadDate(it.timestamp)
            if (!dates.contains(date)) {
                result.add(ThreadPost(isDate = true, date = date))
            }
            dates.add(date)
            result.add(it.posts[0])
        }

        return result
    }


    fun makeThreadScreenshot(view: View) {
        val context = view.context
        val bitmap = loadBitmapFromView(view)
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "2ch screenshot", "");
    }


    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    suspend fun downloadAll(threadNum: String, board: String, context: Context) {
        val photoLinks = getAllPhotos(threadNum, board)
        try {
            photoLinks.forEach {
                download(it, context)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private suspend fun getAllPhotos(threadNum: String, board: String): ArrayList<String> {

        val photoLinks = ArrayList<String>()
        val posts = retrofit.dvach.getPosts(
            "get_thread", board, threadNum, 1
        )
        posts.forEach {
            it.files.forEach { file ->
                photoLinks.add("https://2ch.hk${file.path}")
            }
        }
        return photoLinks
    }

    private fun download(url: String, context: Context) {
        val request = DownloadManager.Request(Uri.parse(url))
        val prefix = if (url.endsWith(".mp4") || url.endsWith("webm")) ".mp4" else ".jpg"
        val file = createFile(prefix)
        val title = if (prefix == ".mp4") "video" else "photo"


        request.apply {
            setTitle("2ch $title")
            setDescription(url)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            allowScanningByMediaScanner()
            setDestinationUri(Uri.fromFile(file))
        }

        val manager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun createFile(prefix: String): File {
        val root: String = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/2ch")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val name = System.currentTimeMillis().toString() + prefix
        return File(myDir, name)
    }


}

