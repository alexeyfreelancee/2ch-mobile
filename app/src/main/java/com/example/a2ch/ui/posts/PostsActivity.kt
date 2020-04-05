package com.example.a2ch.ui.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.R
import com.example.a2ch.adapters.PostListAdapter
import com.example.a2ch.databinding.ActivityPostsBinding
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.ui.make_post.MakePostActivity
import com.example.a2ch.ui.posts.additional.ViewContentFragment
import com.example.a2ch.ui.posts.additional.ViewPostDialog
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

    private lateinit var scrollDown: MenuItem
    private lateinit var scrollUp: MenuItem

    private lateinit var addToFavourites: MenuItem
    private lateinit var removeFromFavourites: MenuItem

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
            threadNum = this@PostsActivity.thread
        }
        viewModel.loadPosts(SwipyRefreshLayoutDirection.TOP)
        viewModel.checkFavourite()
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
            initError(this, it)
        })
        viewModel.openPostDialog.observe(this, Observer {
            openPostDialog(it.peekContent())
        })
        viewModel.openWebLink.observe(this, Observer {
            val url = it.peekContent()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
        viewModel.isFavourite.observe(this, Observer {
            if(it){
                log("visible")
                removeFromFavourites.isVisible = true
                addToFavourites.isVisible = false
            } else{
                log("gone")
                removeFromFavourites.isVisible = false
                addToFavourites.isVisible = true
            }
        })
    }


    private fun openContentDialog(urls: ArrayList<String>, position: Int) {
        val urlsResult = StringBuilder()
        urls.forEach {
            urlsResult.append("${it},")
        }
        log(urlsResult)
        val fragment = ViewContentFragment.newInstance(
            urlsResult.toString(), position
        )

      //  supportFragmentManager.beginTransaction().add(R.id.view_content_frame,fragment).addToBackStack("2ch").commit()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_addFavourites -> {
                viewModel.addToFavourites()
                removeFromFavourites.isVisible = true
                addToFavourites.isVisible = false
                toast("Тред добавлен в избранное")
            }
            R.id.opt_removeFavourites ->{
                viewModel.removeFromFavourites()
                removeFromFavourites.isVisible = false
                addToFavourites.isVisible = true
                toast("Тред удален из избранного")
            }
            R.id.opt_addPost -> {
                startAddPostActivity()
            }
            R.id.opt_scroll_down ->{
                scrollDown()
            }
            R.id.opt_scroll_up ->{
                scrollUp()
            }

        }
        return super.onOptionsItemSelected(item)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.posts_menu, menu)
        scrollUp = menu!!.findItem(R.id.opt_scroll_up)
        scrollUp.isVisible = false
        scrollDown =  menu.findItem(R.id.opt_scroll_down)
        addToFavourites = menu.findItem(R.id.opt_addFavourites)
        removeFromFavourites = menu.findItem(R.id.opt_removeFavourites)
        return true
    }

    private fun openPostDialog(post: ThreadPost) {
        val dialog = ViewPostDialog(
            this, post, viewModel
        )
        dialog.show()
    }


    private fun startAddPostActivity() {
        startActivity(
            Intent(applicationContext, MakePostActivity::class.java)
                .putExtra(BOARD_NAME, board)
                .putExtra(THREAD_NUM, thread)
        )
    }

    private fun initSwipeToRefresh() {
        swipe_refresh.setOnRefreshListener { direction ->
            viewModel.loadPosts(direction)
        }
    }


    private fun initPostList() {
        postListAdapter.listener = this
        post_list.adapter = postListAdapter
    }


    private fun scrollDown() {
        post_list.layoutManager?.scrollToPosition(postListAdapter.itemCount - 1)
        scrollDown.isVisible = false
        scrollUp.isVisible = true
    }

    private fun scrollUp() {
        post_list.layoutManager?.scrollToPosition(0)
        scrollDown.isVisible = true
        scrollUp.isVisible = false
    }

    override fun upReached() {
        scrollUp.isVisible = false
        scrollDown.isVisible = true
    }

    override fun bottomReached() {
        scrollUp.isVisible = true
        scrollDown.isVisible = false
    }
    override fun onResume() {
        viewModel.loadPosts(SwipyRefreshLayoutDirection.TOP)
        super.onResume()
    }
}
