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
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.a2ch.R
import com.example.a2ch.adapters.PostListAdapter
import com.example.a2ch.databinding.ActivityPostsBinding
import com.example.a2ch.ui.send_post.SendPostActivity
import com.example.a2ch.util.BOARD_NAME
import com.example.a2ch.util.THREAD_NUM
import com.example.a2ch.util.gone
import com.example.a2ch.util.visible
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
        viewModel.openDialog.observe(this, Observer {
            openPhotoDialog(it.peekContent())
        })
    }


    private fun openPhotoDialog(url: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_photo)
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

        dialog.show()
    }

    private fun setupImage(image: ImageView, download: ImageView, url: String) {
        Glide.with(applicationContext)
            .load(url)
            .into(image)

        download.setOnClickListener {
            downloadPhoto("https://2ch.hk$url")
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
            downloadVideo(url)
        }
    }


    private fun downloadPhoto(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        val file = createFile("jpg")

        request.apply {
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        or DownloadManager.Request.NETWORK_MOBILE
            )
            setTitle("Загузка видео")
            setDescription(url)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            allowScanningByMediaScanner()
            setDestinationUri(Uri.fromFile(file))
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

    }

    private fun downloadVideo(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        val file = createFile("mp4")

        request.apply {
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        or DownloadManager.Request.NETWORK_MOBILE
            )
            setTitle("Загузка видео")
            setDescription(url)
            allowScanningByMediaScanner()
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationUri(Uri.fromFile(file))
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
