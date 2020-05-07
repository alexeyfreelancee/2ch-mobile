package com.alexey_vena.a2ch.ui.posts.dialogs


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.pictures.ViewPicsActivity
import com.alexey_vena.a2ch.ui.posts.PostsViewModel
import com.alexey_vena.a2ch.util.*
import com.bumptech.glide.Glide
import org.sufficientlysecure.htmltextview.HtmlTextView


class ViewPostDialog(
    private val ctx: Context,
    private val post: ThreadPost,
    private val viewModel: PostsViewModel
) : Dialog(ctx) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.dialog_post)

        val parent = findViewById<ScrollView>(R.id.parent)
        val name = findViewById<HtmlTextView>(R.id.name)
        val num = findViewById<TextView>(R.id.num)
        val date = findViewById<TextView>(R.id.date)
        val comment = findViewById<HtmlTextView>(R.id.comment)
        val photo1 = findViewById<ImageView>(R.id.photo1)
        val photo2 = findViewById<ImageView>(R.id.photo2)
        val photo3 = findViewById<ImageView>(R.id.photo3)
        val photo4 = findViewById<ImageView>(R.id.photo4)
        val photo5 = findViewById<ImageView>(R.id.photo5)
        val photo6 = findViewById<ImageView>(R.id.photo6)
        val photo7 = findViewById<ImageView>(R.id.photo7)
        val photo8 = findViewById<ImageView>(R.id.photo8)

        comment.visibility = if (post.comment.isEmpty()) View.GONE else View.VISIBLE
        name.visibility = if (post.name.isEmpty()) View.GONE else View.VISIBLE

        photo1.visibility = if (post.files.isNotEmpty()) View.VISIBLE else View.GONE
        photo2.visibility = if (post.files.size > 1) View.VISIBLE else View.GONE
        photo3.visibility = if (post.files.size > 2) View.VISIBLE else View.GONE
        photo4.visibility = if (post.files.size > 3) View.VISIBLE else View.GONE
        photo5.visibility = if (post.files.size > 4) View.VISIBLE else View.GONE
        photo6.visibility = if (post.files.size > 5) View.VISIBLE else View.GONE
        photo7.visibility = if (post.files.size > 6) View.VISIBLE else View.GONE
        photo8.visibility = if (post.files.size > 7) View.VISIBLE else View.GONE

        name.setHtml(post.name)
        num.text = "#${post.num}"
        date.text = getDate(post.timestamp)
        setTextViewHTML(comment, post.comment, viewModel)

        comment.setOnClickATagListener { widget, href ->
            if (href != null) viewModel.openUrl(href)
        }

        photo1.setOnClickListener {
            showContent(post, 0)
        }
        photo2.setOnClickListener {
            showContent(post, 1)
        }
        photo3.setOnClickListener {
            showContent(post, 2)
        }
        photo4.setOnClickListener {
            showContent(post, 3)
        }
        photo5.setOnClickListener {
            showContent(post, 4)
        }
        photo6.setOnClickListener {
            showContent(post, 5)
        }
        photo7.setOnClickListener {
            showContent(post, 6)
        }
        photo8.setOnClickListener {
            showContent(post, 7)
        }

        try {
            Glide.with(ctx)
                .load("https://2ch.hk${post.files[0].thumbnail}")
                .apply(myOptions)
                .into(photo1)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[1].thumbnail}")
                .apply(myOptions)
                .into(photo2)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[2].thumbnail}")
                .apply(myOptions)
                .into(photo3)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[3].thumbnail}")
                .apply(myOptions)
                .into(photo4)
            Glide.with(ctx)
                .load("https://2ch.hk${post.files[4].thumbnail}")
                .apply(myOptions)
                .into(photo5)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[5].thumbnail}")
                .apply(myOptions)
                .into(photo6)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[6].thumbnail}")
                .apply(myOptions)
                .into(photo7)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[7].thumbnail}")
                .apply(myOptions)
                .into(photo8)
        } catch (ex: Exception) {
        }

        parent.setOnLongClickListener {
            makeViewScreenshot(parent)
            return@setOnLongClickListener true
        }

        comment.setOnLongClickListener() {
            makeViewScreenshot(parent)
            return@setOnLongClickListener true
        }
    }

    private fun makeViewScreenshot(view: View) {
        val context = view.context
        val bitmap = loadBitmapFromView(view)
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "2ch screenshot", "");
        context.toast("Скриншот сохранен в галерею")
    }


    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    private fun showContent(post: ThreadPost, position: Int) {
        val urls = arrayListOf<String>()
        post.files.forEach {
            urls.add("https://2ch.hk${it.path}")
        }

        val urlsResult = StringBuilder()
        urls.forEach {
            urlsResult.append("${it},")
        }

        context.startActivity(
            Intent(context, ViewPicsActivity::class.java)
                .putExtra(URLS, urlsResult.toString())
                .putExtra(POSITION, position)
        )

    }


}