package com.example.a2ch.ui.posts

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.a2ch.R
import com.example.a2ch.adapters.PostListAdapter
import com.example.a2ch.databinding.ActivityPostsBinding
import com.example.a2ch.databinding.DialogPostBinding
import com.example.a2ch.models.post.Post
import com.example.a2ch.ui.send_post.SendPostActivity
import com.example.a2ch.util.*
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.android.synthetic.main.activity_posts.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.File

class PostsActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: PostsViewModelFactory by instance()
    private lateinit var viewModel: PostsViewModel
    private lateinit var postListAdapter: PostListAdapter

    private var board = ""
    private var thread = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(PostsViewModel::class.java)
        DataBindingUtil.setContentView<ActivityPostsBinding>(this, R.layout.activity_posts).apply {
            viewmodel = viewModel
            lifecycleOwner = this@PostsActivity
        }
        postListAdapter = PostListAdapter(viewModel)

        initSwipeToRefresh()
        initViewModelData()
        initObservers()
        initPostList()
    }

    private fun initViewModelData() {
        board = intent.getStringExtra(BOARD_NAME)
        thread = intent.getStringExtra(THREAD_NUM)
        viewModel.apply {
            board = this@PostsActivity.board
            thread = this@PostsActivity.thread
        }
        viewModel.loadPosts(SwipyRefreshLayoutDirection.TOP)
    }

    private fun initSwipeToRefresh() {
        swipe_refresh.setOnRefreshListener {
            viewModel.loadPosts(it)
        }
    }

    private fun initObservers() {
        viewModel.posts.observe(this, Observer {
            postListAdapter.updateList(it)
        })
        viewModel.scrollToBottom.observe(this, Observer {
            scrollToBottom()
        })
        viewModel.openPhotoDialog.observe(this, Observer {
            openPhotoDialog(it.peekContent())
        })
        viewModel.error.observe(this, Observer {
            toast(it)
            finish()
        })
        viewModel.openPostDialog.observe(this, Observer {
            openPostDialog(it.peekContent())
        })
    }

    private fun openPostDialog(post: Post) {
        val dialog = Dialog(this)

        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_post)
            show()
        }

        val name = dialog.findViewById<TextView>(R.id.name)
        val num = dialog.findViewById<TextView>(R.id.num)
        val date = dialog.findViewById<TextView>(R.id.date)
        val comment = dialog.findViewById<TextView>(R.id.comment)
        val parent = dialog.findViewById<TextView>(R.id.parent)
        val photo1 = dialog.findViewById<ImageView>(R.id.photo1)
        val photo2 = dialog.findViewById<ImageView>(R.id.photo2)
        val photo3 = dialog.findViewById<ImageView>(R.id.photo3)
        val photo4 = dialog.findViewById<ImageView>(R.id.photo4)

        parent.visibility = if (post.op == 1 || post.parent == "0") View.INVISIBLE else View.VISIBLE
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
        parent.text = post.parent

        parent.setOnClickListener {
            viewModel.openPostDialog(post.parent)
        }

        photo1.setOnClickListener {
            openPhotoDialog(post.files[0].path)
        }
        photo2.setOnClickListener {
            openPhotoDialog(post.files[1].path)
        }
        photo3.setOnClickListener {
            openPhotoDialog(post.files[2].path)
        }
        photo4.setOnClickListener {
            openPhotoDialog(post.files[3].path)
        }

        try {
            Glide.with(applicationContext)
                .load("https://2ch.hk${post.files[0].path}")
                .into(photo1)

            Glide.with(applicationContext)
                .load("https://2ch.hk${post.files[1].path}")
                .into(photo2)

            Glide.with(applicationContext)
                .load("https://2ch.hk${post.files[2].path}")
                .into(photo3)

            Glide.with(applicationContext)
                .load(post.files[3]).load("https://2ch.hk${post.files[3].path}")
                .into(photo4)
        } catch (ex: Exception){}


    }

    private fun openPhotoDialog(url: String) {
        val dialog = Dialog(this)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_photo)
            show()
        }

        val image = dialog.findViewById(R.id.photo) as ImageView
        val download = dialog.findViewById(R.id.download) as ImageView
        val video = dialog.findViewById<VideoView>(R.id.video)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progress)

        if (url.endsWith("mp4") || url.endsWith("webm")) {
            image.gone()
            video.visible()
            progressBar.visible()
            setupVideo(video, download, "https://2ch.hk$url", progressBar)
        } else {
            image.visible()
            video.gone()
            progressBar.gone()
            setupImage(image, download, "https://2ch.hk$url")
        }
    }

    private fun setupImage(image: ImageView, download: ImageView, url: String) {
        Glide.with(applicationContext)
            .load(url)
            .into(image)

        download.setOnClickListener {
            download(url, ".jpg")
        }
    }

    private fun setupVideo(
        video: VideoView,
        download: ImageView,
        url: String,
        progressBar: ProgressBar
    ) {
        video.setVideoPath(url)
        video.requestFocus()
        progressBar.visible()
        video.setOnPreparedListener {
            progressBar.gone()
            video.start()
        }

        download.setOnClickListener {
            download(url, ".mp4")
        }
    }


    private fun download(url: String, prefix: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        val file = createFile(prefix)
        val title = if (prefix == ".mp4") "video" else "photo"


        request.apply {
            setTitle("2ch $title")
            setDescription(url)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            allowScanningByMediaScanner()
            setDestinationUri(Uri.fromFile(file))
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        toast("Загрузка началась")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_add_post -> {
                startActivity(
                    Intent(applicationContext, SendPostActivity::class.java)
                        .putExtra(BOARD_NAME, board)
                        .putExtra(THREAD_NUM, thread)
                )
            }
            R.id.opt_bottom -> scrollToBottom()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.posts_menu, menu)
        return true
    }


    private fun initPostList() {
        post_list.adapter = postListAdapter

    }

    private fun scrollToBottom() {
        post_list.layoutManager?.scrollToPosition(postListAdapter.itemCount - 1)
    }
}
