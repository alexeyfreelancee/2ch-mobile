package com.example.a2ch.ui.posts

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.R
import com.example.a2ch.adapters.PostListAdapter
import com.example.a2ch.databinding.ActivityPostsBinding
import com.example.a2ch.models.post.Post
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.Error
import com.example.a2ch.ui.posts.dialogs.ViewContentDialog
import com.example.a2ch.ui.posts.dialogs.ViewPostDialog
import com.example.a2ch.ui.make_post.MakePostActivity
import com.example.a2ch.util.*
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.android.synthetic.main.activity_posts.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class PostsActivity : AppCompatActivity(), KodeinAware,
    PostsAdapterListener {

    override val kodein by kodein()
    private val factory: PostsViewModelFactory by instance()
    private lateinit var viewModel: PostsViewModel
    private lateinit var postListAdapter: PostListAdapter

    private var board = ""
    private var thread = ""

    private var needToAnimate = false
    private var positionUp = true

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
        initActionBar()
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


    private fun initObservers() {
        viewModel.posts.observe(this, Observer {
            postListAdapter.updateList(it)
        })
        viewModel.scrollToBottom.observe(this, Observer {
            scrollDown()
        })
        viewModel.contentDialogData.observe(this, Observer {
            val data = it.peekContent()
            openContentDialog(data.urls, data.position)
        })
        viewModel.error.observe(this, Observer {
            initError(this,it)
        })
        viewModel.openPostDialog.observe(this, Observer {
            openPostDialog(it.peekContent())
        })
        viewModel.openWebLink.observe(this, Observer {
            val url = it.peekContent()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
    }

    private fun openPostDialog(post: Post) {
        val dialog = ViewPostDialog(
            this, post, viewModel
        )
        dialog.show()
    }

    private fun openContentDialog(urls: ArrayList<String>, position: Int) {
        val dialog = ViewContentDialog(
            this,
            urls,
            position
        )

        dialog.show()
    }


    private fun initActionBar() {
        opt_add_post.setOnClickListener {
            startAddPostActivity()
        }

        scroll.setOnClickListener {
            if (positionUp) {
                scrollDown()
                bottomReached()
            } else {
                scrollUp()
                upReached()
            }
        }
    }


    override fun upReached() {
        positionUp = true
        val animator = ObjectAnimator.ofFloat(scroll, View.ROTATION, 180f, 0f)
        if (needToAnimate) {
            animator.start()
        }
        needToAnimate = true
    }

    override fun bottomReached() {
        positionUp = false
        val animator = ObjectAnimator.ofFloat(scroll, View.ROTATION, 0f, 180f)
        if (needToAnimate) {
            animator.start()
        }
        needToAnimate = true
    }

    private fun startAddPostActivity() {
        startActivity(
            Intent(applicationContext, MakePostActivity::class.java)
                .putExtra(BOARD_NAME, board)
                .putExtra(THREAD_NUM, thread)
        )
    }

    private fun initSwipeToRefresh() {
        swipe_refresh.setOnRefreshListener {direction->
            viewModel.loadPosts(direction)
        }
    }


    private fun initPostList() {
        postListAdapter.listener = this
        post_list.adapter = postListAdapter
    }


    private fun scrollDown() {
        post_list.layoutManager?.scrollToPosition(postListAdapter.itemCount - 1)
    }

    private fun scrollUp() {
        post_list.layoutManager?.scrollToPosition(0)
    }


    override fun onResume() {
        viewModel.loadPosts(SwipyRefreshLayoutDirection.TOP)
        super.onResume()
    }
}
