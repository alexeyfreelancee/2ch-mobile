package com.example.a2ch.ui.posts

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.a2ch.R
import com.example.a2ch.models.post.Post

class ViewPostDialog(
    private val ctx: Context,
    private val post: Post,
    private val viewModel: PostsViewModel
) : Dialog(ctx) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.dialog_post)

        val name = findViewById<TextView>(R.id.name)
        val num = findViewById<TextView>(R.id.num)
        val date = findViewById<TextView>(R.id.date)
        val comment = findViewById<TextView>(R.id.comment)
        val parent = findViewById<TextView>(R.id.parent)
        val photo1 = findViewById<ImageView>(R.id.photo1)
        val photo2 = findViewById<ImageView>(R.id.photo2)
        val photo3 = findViewById<ImageView>(R.id.photo3)
        val photo4 = findViewById<ImageView>(R.id.photo4)

        parent.visibility = if (post.op == 1 || post.parent.isEmpty()) View.GONE else View.VISIBLE
        comment.visibility = if (post.comment.isEmpty()) View.GONE else View.VISIBLE
        name.visibility = if (post.name.isEmpty()) View.GONE else View.VISIBLE

        photo1.visibility = if (post.files.isNotEmpty()) View.VISIBLE else View.GONE
        photo2.visibility = if (post.files.size > 1) View.VISIBLE else View.GONE
        photo3.visibility = if (post.files.size > 2) View.VISIBLE else View.GONE
        photo4.visibility = if (post.files.size > 3) View.VISIBLE else View.GONE

        name.text = post.name
        num.text = "#${post.num}"
        date.text = post.date
        comment.text = post.comment
        parent.text = ">>${post.parent}"

        parent.setOnClickListener {
            viewModel.openPostDialog(post.parent)
        }

        photo1.setOnClickListener {
            openPhotoDialog("https://2ch.hk${post.files[0].path}")
        }
        photo2.setOnClickListener {
            openPhotoDialog("https://2ch.hk${post.files[1].path}")
        }
        photo3.setOnClickListener {
            openPhotoDialog("https://2ch.hk${post.files[2].path}")
        }
        photo4.setOnClickListener {
            openPhotoDialog("https://2ch.hk${post.files[3].path}")
        }

        try {
            Glide.with(ctx)
                .load("https://2ch.hk${post.files[0].path}")
                .into(photo1)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[1].path}")
                .into(photo2)

            Glide.with(ctx)
                .load("https://2ch.hk${post.files[2].path}")
                .into(photo3)

            Glide.with(ctx)
                .load(post.files[3]).load("https://2ch.hk${post.files[3].path}")
                .into(photo4)
        } catch (ex: Exception) {
        }


    }

    private fun openPhotoDialog(url: String) {
        val dialog = ViewContentDialog(ctx, url)
        dialog.show()
    }
}