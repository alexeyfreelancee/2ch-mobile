package com.example.a2ch.data

import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.captcha.CaptchaInfo
import com.example.a2ch.models.category.BoardInfo
import com.example.a2ch.models.post.MakePostError
import com.example.a2ch.models.post.Post
import com.example.a2ch.util.getDate
import com.example.a2ch.util.parseDigits
import org.jsoup.Jsoup


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


    suspend fun loadPosts(thread: String, board: String): List<Post> {
        val posts = retrofit.dvach.getPosts(
            "get_thread", board, thread, 1
        )
        preparePosts(posts)
        return posts
    }


    suspend fun getPost(board: String, postId: String): Post {
        val posts = retrofit.dvach.getCurrentPost("get_post", board, postId)

        preparePosts(posts)
        return posts[0]
    }


    private fun preparePosts(posts: List<Post>) {
        posts.forEach {
            prepareCurrentPost(it)
        }
    }


    private fun prepareCurrentPost(post: Post) {
        //Парсим нужное значение из html
        val doc = Jsoup.parse(post.comment)
        val htmlTagA = doc.getElementsByTag("a")

        //Среди зачений могут быть ссылки, проверяем чтобы оставались только номера постов
        val iterator = htmlTagA.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (!(item.text().matches(">>\\d+".toRegex())
                        || item.text().matches(">>\\d+ \\(OP\\)".toRegex()))
            ) {
                iterator.remove()
            }
        }

        //Если пост является ответом, надо задать значение parent
        if (htmlTagA.size == 1) {
            post.parent = htmlTagA[0].text().parseDigits()
        } else {
            post.parent = ""
        }

        //Удаляем тег "а", тк в нем ненужное говно
        doc.select("a").remove()

        //Придаем данныйм презентабельный вид
        post.comment = doc.text()
        post.date = getDate(post.timestamp)
        post.name = stripHtml(post.name)
    }


    private fun stripHtml(html: String): String {
        return Jsoup.parse(html).text()
    }

}

