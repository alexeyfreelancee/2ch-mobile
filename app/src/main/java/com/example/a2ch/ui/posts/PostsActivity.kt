package com.example.a2ch.ui.posts

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.Transition
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.a2ch.R
import com.example.a2ch.adapters.PostListAdapter
import com.example.a2ch.databinding.ActivityPostsBinding
import com.example.a2ch.util.CATEGORY_NAME
import com.example.a2ch.util.THREAD_NUM
import com.example.a2ch.util.toast
import kotlinx.android.synthetic.main.activity_posts.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileOutputStream

class PostsActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: PostsViewModelFactory by instance()
    private lateinit var viewModel: PostsViewModel
    private lateinit var postListAdapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(PostsViewModel::class.java)
        DataBindingUtil.setContentView<ActivityPostsBinding>(this, R.layout.activity_posts).apply {
            viewmodel = viewModel
            lifecycleOwner = this@PostsActivity
        }
        postListAdapter = PostListAdapter(viewModel)

        viewModel.apply {
            board = intent.getStringExtra(CATEGORY_NAME)
            thread = intent.getStringExtra(THREAD_NUM)
        }
        viewModel.loadPosts()


        initObservers()
        initPostList()
    }

    private fun initObservers() {
        viewModel.posts.observe(this, Observer {
            postListAdapter.updateList(it)
        })
        viewModel.openDialog.observe(this, Observer {
            openPhotoDialog(it.peekContent())
        })
    }

    private fun openPhotoDialog(photoUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_photo)
        val image = dialog.findViewById(R.id.photo) as ImageView
        val download = dialog.findViewById(R.id.download) as ImageView

        Glide.with(applicationContext)
            .load("https://2ch.hk$photoUrl")
            .into(image)

        download.setOnClickListener{
            Glide.with(this)
                .asBitmap()
                .load("https://2ch.hk$photoUrl")
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                      downloadPhoto(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        }
        dialog.show()
    }

    fun downloadPhoto(bitmap: Bitmap){
        try {
            val root: String = Environment.getExternalStorageDirectory().toString()
            val myDir = File("$root/2ch")
            if (!myDir.exists()) {
                myDir.mkdirs()
            }
            val name = System.currentTimeMillis().toString() + ".jpg"
            val f = File(myDir, name)
            val out = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)))
            out.flush()
            out.close()
            toast("Изображение загружено")
        } catch (e: Exception) {
            e.printStackTrace()
            toast("Ошибка")
        }
    }

    private fun initPostList() {
        post_list.adapter = postListAdapter
    }
}
