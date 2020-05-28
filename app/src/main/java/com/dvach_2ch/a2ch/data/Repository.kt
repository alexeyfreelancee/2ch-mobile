package com.dvach_2ch.a2ch.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.view.View
import com.dvach_2ch.a2ch.data.db.AppDatabase
import com.dvach_2ch.a2ch.data.networking.RetrofitClient
import com.dvach_2ch.a2ch.models.Thumbnail
import com.dvach_2ch.a2ch.models.boards.BoardsBase
import com.dvach_2ch.a2ch.models.threads.ThreadBase
import com.dvach_2ch.a2ch.models.threads.ThreadItem
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.util.isNetworkAvailable
import com.dvach_2ch.a2ch.util.isWebLink
import com.dvach_2ch.a2ch.util.parseDigits
import com.dvach_2ch.a2ch.util.parseThreadDate
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class Repository(
    private val retrofit: RetrofitClient,
    private val db: AppDatabase,
    private val prefsHelper: SharedPrefsHelper
) {

    suspend fun loadAnswers(
        threadNum: String,
        board: String,
        mainPostNum: String
    ): List<ThreadPost> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val posts = loadPosts(threadNum, board)
            val resultList = ArrayList<ThreadPost>()
            posts.forEach {
                val comment = Html.fromHtml(it.comment).toString()
                if (comment.contains(mainPostNum)) resultList.add(it)
            }
            resultList
        }

    }

    suspend fun loadBoards(): BoardsBase {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            retrofit.dvach.getBoards()
        }
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

    suspend fun makePost(
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
        username: String,
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

    private fun getPostNum(href: String): String {
        return href
            .substring(href.lastIndexOf("#") + 1)
            .parseDigits()
    }

    suspend fun getPost(href: String, threadNum: String): ThreadPost {
        //href example /b/res/216879164.html#216879164\
        val boardId = href.split("/")[1]
        val postId = getPostNum(href)

        val dbPost = CoroutineScope(Dispatchers.IO).async {
            val dbThread = db.threadDao().getThread(boardId, threadNum)
            dbThread?.posts?.find { post ->
                post.num == postId
            }

        }
        var networkPost: Deferred<ThreadPost?>? = null

        if (isNetworkAvailable() && dbPost.await() == null) {
            networkPost = CoroutineScope(Dispatchers.IO).async {
                retrofit.dvach.getCurrentPost("get_post", boardId, postId)[0]
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

    suspend fun loadPosts(threadNum: String, board: String): List<ThreadPost> {
        val thread = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            if (isNetworkAvailable()) {
                addPostsToDb(board, threadNum)
            }

            db.threadDao().getThread(board, threadNum)
        } ?: loadBoardInfo(board)?.threadItems?.find { it.num == threadNum }
        thread?.countAnswers()
        return thread?.posts ?: emptyList()
    }

    private fun ThreadItem.countAnswers() {
        val answersMap = HashMap<String, Int>()
        val thread = this
        thread.posts.forEach { post ->
            post.countMentions(answersMap)
        }
        thread.posts.map { post -> post.answers = answersMap[post.num] ?: 0 }
    }

    private fun ThreadPost.countMentions(answersMap: HashMap<String, Int>) {
        val spanned = Html.fromHtml(this.comment)
        val urls = SpannableStringBuilder(spanned).getSpans(0, spanned.length, URLSpan::class.java)

        urls.forEach {
            if (!it.url.isWebLink()) {
                val num = getPostNum(it.url)
                val beforeCount = answersMap[num] ?: 0
                answersMap[num] = beforeCount + 1
            }
        }
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

    suspend fun removeFromFavourites(threadItem: ThreadItem) {
        threadItem.isFavourite = false
        db.threadDao().saveWithTimestamp(threadItem)
    }

    suspend fun loadFavourites(): List<ThreadPost> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val threads = db.threadDao().getFavouriteThreads()
            val result = ArrayList<ThreadPost>()
            threads.forEach {
                result.add(it.posts[0])
            }
            result
        }

    }

    suspend fun loadHistory(): List<ThreadPost> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
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

            result
        }

    }


    fun makeThreadScreenshot(view: View) {
        val context = view.context
        val bitmap = loadBitmapFromView(view)
        val file = createFile(".jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        context.sendBroadcast(mediaScanIntent)
    }


    private fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    suspend fun downloadAll(threadNum: String, board: String, context: Context) {
        val photoLinks = loadAllPhotos(threadNum, board)
        try {
            photoLinks.forEach {
                download(it, context)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    suspend fun loadAllThumbs(threadNum: String, board: String): ArrayList<Thumbnail> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val thumbs = ArrayList<Thumbnail>()
            val posts = retrofit.dvach.getPosts(
                "get_thread", board, threadNum, 1
            )
            posts.forEach {
                it.files.forEach { file ->
                    val lastIndex = file.path.lastIndexOf(".") + 1
                    val postfix = file.path.substring(lastIndex, file.path.length)
                    thumbs.add(Thumbnail(postfix, file.thumbnail ?: ""))
                }
            }
            thumbs
        }

    }

    suspend fun loadAllPhotos(threadNum: String, board: String): ArrayList<String> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val photoLinks = ArrayList<String>()
            val posts = retrofit.dvach.getPosts(
                "get_thread", board, threadNum, 1
            )
            posts.forEach {
                it.files.forEach { file ->
                    photoLinks.add("https://2ch.hk${file.path}")
                }
            }
            photoLinks
        }

    }

    private fun download(url: String, context: Context) {
        val request = DownloadManager.Request(Uri.parse(url))
        val prefix = if (url.endsWith("mp4") || url.endsWith("webm")) ".mp4" else ".jpg"
        val file = createFile(prefix)
        val title = if (prefix == ".mp4") "video" else "photo"
        val uri = Uri.fromFile(file)

        request.apply {
            setTitle("2ch $title")
            setDescription(url)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            allowScanningByMediaScanner()
            setDestinationUri(uri)
        }


        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)

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

