package com.alexey_vena.a2ch.data

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.alexey_vena.a2ch.data.db.AppDatabase
import com.alexey_vena.a2ch.data.networking.RetrofitClient
import com.alexey_vena.a2ch.models.boards.BoardsBase
import com.alexey_vena.a2ch.models.threads.ThreadBase
import com.alexey_vena.a2ch.models.threads.ThreadItem
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.util.isNetworkAvailable
import com.alexey_vena.a2ch.util.parseDigits
import com.alexey_vena.a2ch.util.parseThreadDate
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class Repository(
    private val retrofit: RetrofitClient,
    private val db: AppDatabase,
    private val prefsHelper: SharedPrefsHelper
) {

    suspend fun loadBoards(): BoardsBase {
        val result = CoroutineScope(Dispatchers.IO).async {
            retrofit.dvach.getBoards()
        }
        return result.await()
    }

    suspend fun loadBoardInfo(board: String): ThreadBase? {
        return try {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                retrofit.dvach.getThreads(board)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }

    }

    fun loadUsername(): String {
        return prefsHelper.loadUsername() ?: ""
    }

    suspend fun makePostWithCaptcha(
        board: String,
        thread: String,
        comment: String,
        captchaKey: String,
        captchaResponse: String,
        username: String
    ): String {
        if (loadBoardInfo(board)?.threadItems?.find { it.num == thread } == null) {
            return "Тред умер"
        }
        try {
            val urlParameters = buildPostUrl(
                board = board,
                thread = thread,
                captchaKey = captchaKey,
                comment = java.net.URLDecoder.decode(comment, "utf-8"),
                captchaResponse = captchaResponse,
                username = username
            )
            val postData = urlParameters.toByteArray(StandardCharsets.UTF_8)
            val request = "https://2ch.hk/makaba/posting.fcgi"
            val conn = URL(request).openConnection() as HttpURLConnection
            conn.apply {
                doOutput = true
                instanceFollowRedirects = false
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("charset", "utf-8")
                setRequestProperty("Content-Length", postData.size.toString())
                useCaches = false
            }

            DataOutputStream(conn.outputStream).use { wr -> wr.write(postData) }
            prefsHelper.saveUsername(username)
            return if (conn.responseCode == 200) "OK" else conn.responseMessage


        } catch (ex: Exception) {
            ex.printStackTrace()
            return ex.localizedMessage
        }
    }

    private fun buildPostUrl(
        json: Int = 1,
        task: String = "post",
        username:String,
        board: String,
        thread: String,
        captchaType: String = "recaptcha",
        captchaKey: String,
        captchaResponse: String,
        comment: String
    ): String {
        return "json=$json&task=$task&board=$board" +
                "&thread=$thread&captcha_type=$captchaType" +
                "&captcha-key=$captchaKey" +
                "&g-recaptcha-response=$captchaResponse&comment=$comment&name=$username"
    }


    suspend fun getCaptchaKey(board: String, thread: String): String {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            retrofit.dvach.getCaptchaId(board, thread).id
        }
    }

    suspend fun isFavourite(board: String, threadNum: String): Boolean {
        val thread = CoroutineScope(Dispatchers.IO).async {
            getThread(board, threadNum)
        }
        return thread.await()?.isFavourite ?: false

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

    private suspend fun addPostsToDb(board: String, threadNum: String) {
        val thread = getThread(board, threadNum)
        val posts = try {
            retrofit.dvach.getPosts(
                "get_thread", board, threadNum, 1
            )
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }

        if (thread != null && posts != null) {
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

    suspend fun getThreadFromDb(board: String, threadNum: String): ThreadItem? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.threadDao().getThread(board, threadNum)
        }
    }

    suspend fun loadPosts(thread: String, board: String): List<ThreadPost> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            if (isNetworkAvailable()) {
                addPostsToDb(board, thread)
            }
            val threads = db.threadDao().getThread(board, thread)
            threads
        }?.posts ?: getThread(board, thread)!!.posts
    }

    suspend fun getThread(board: String, threadNum: String): ThreadItem? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.threadDao().getThread(board, threadNum)
        } ?: loadBoardInfo(board)?.threadItems?.find { it.num == threadNum }
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
        val thread = getThread(threadPost.board, threadPost.num)

        thread?.let {
            thread.isFavourite = false
            db.threadDao().saveWithTimestamp(thread)
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
                    db.threadDao().updateThread(thread)
                }
            }

        } catch (ex: Exception) {
        }

    }

    suspend fun computeUnreadPosts(threadNum: String, board: String): Int? {
        val thread = db.threadDao().getThread(board, threadNum)
        var postsWereRead = 0
        thread?.posts?.forEach {
            if (it.isRead) postsWereRead++
        }

        val postsSaved = thread?.posts?.size ?: 0
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
            val date = parseThreadDate(it.saveTime)
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

