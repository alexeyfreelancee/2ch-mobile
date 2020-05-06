package com.alexey_vena.a2ch.ui.posts.dialogs


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.posts.PostsViewModel
import com.alexey_vena.a2ch.ui.pictures.ViewPicsActivity
import com.alexey_vena.a2ch.util.*
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

        val parent = findViewById<RelativeLayout>(R.id.parent)
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
        photo1.setOnClickListener {
            showContent(post, 4)
        }
        photo2.setOnClickListener {
            showContent(post, 5)
        }
        photo3.setOnClickListener {
            showContent(post, 6)
        }
        photo4.setOnClickListener {
            showContent(post, 7)
        }

        try {
            Glide.with(ctx)

                .load("https://2ch.hk${post.files[0].path}")
                .apply(myOptions)
                .into(photo1)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[1].path}")
                .apply(myOptions)
                .into(photo2)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[2].path}")
                .apply(myOptions)
                .into(photo3)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[3].path}")
                .apply(myOptions)
                .into(photo4)
            Glide.with(ctx)
                .load("https://2ch.hk${post.files[4].path}")
                .apply(myOptions)
                .into(photo5)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[5].path}")
                .apply(myOptions)
                .into(photo6)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[6].path}")
                .apply(myOptions)
                .into(photo7)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[7].path}")
                .apply(myOptions)
                .into(photo8)
        } catch (ex: Exception) {
        }

        parent.setOnClickListener {
            makeViewScreenshot(it)
        }

        comment.setOnClickListener {
            makeViewScreenshot(it)
        }
    }

    private fun makeViewScreenshot(view: View) {
        val context = view.context
        val bitmap = loadBitmapFromView(view)
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "2ch screenshot", "");
        vibrate(context)
    }

    private fun vibrate(context: Context) {
        val vibrator = ContextCompat.getSystemService<Vibrator>(context, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    500, VibrationEffect.EFFECT_CLICK
                )
            )
        } else {
            vibrator?.vibrate(500)
        }
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
            Intent(context, ViewPicsActivity::class.java).putExtra(
                URLS,
                urlsResult.toString()
            ).putExtra(
                POSITION, position
            )
        )

    }


}