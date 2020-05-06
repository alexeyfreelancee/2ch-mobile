package com.alexey_vena.a2ch.ui.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.adapters.PostListAdapter
import com.alexey_vena.a2ch.databinding.ActivityPostsBinding
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.make_post.MakePostActivity
import com.alexey_vena.a2ch.ui.pictures.ViewPicsActivity
import com.alexey_vena.a2ch.ui.posts.dialogs.PostActionDialog
import com.alexey_vena.a2ch.ui.posts.dialogs.ViewPostDialog
import com.alexey_vena.a2ch.util.*
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.android.synthetic.main.activity_posts.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class PostsActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: PostsViewModelFactory by instance()
    private lateinit var viewModel: PostsViewModel
    private lateinit var postListAdapter: PostListAdapter

    private var board = ""
    private var thread = ""


    private lateinit var addToFavourites: MenuItem
    private lateinit var removeFromFavourites: MenuItem

    private var unreadPostsCount = 0

    private var recyclerViewState: Parcelable? = null

    private lateinit var binding : ActivityPostsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(PostsViewModel::class.java)
        initObservers()
        binding = DataBindingUtil.setContentView<ActivityPostsBinding>(this, R.layout.activity_posts).apply {
            viewmodel = viewModel
            lifecycleOwner = this@PostsActivity
        }
        postListAdapter = PostListAdapter(viewModel)


        initViewModelData()
        binding.postList.adapter = postListAdapter
    }

    private fun initViewModelData() {
        board = intent.getStringExtra(BOARD_NAME)
        thread = intent.getStringExtra(THREAD_NUM)
        viewModel.apply {
            board = this@PostsActivity.board
            threadNum = this@PostsActivity.thread
        }

        viewModel.getUnreadPosts()
        viewModel.checkFavourite()
    }

    override fun onResume() {
        recyclerViewState = binding.postList.layoutManager?.onSaveInstanceState()
        viewModel.loadPosts()
        super.onResume()
    }

    private fun initObservers() {
        viewModel.posts.observe(this, Observer {
            postListAdapter.updateList(it)
            if(recyclerViewState!=null){
                binding.postList.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        })
        viewModel.contentDialogData.observe(this, Observer {
            val data = it.peekContent()
            showContent(data.urls, data.position)
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
        viewModel.unreadPosts.observe(this, Observer {
            if (it != 0) {
                toast("$it unread")
                unreadPostsCount = it
            }

        })
        viewModel.isFavourite.observe(this, Observer {
            if (it) {
                removeFromFavourites.isVisible = true
                addToFavourites.isVisible = false
            } else {
                removeFromFavourites.isVisible = false
                addToFavourites.isVisible = true
            }
        })
        viewModel.removeFromFavourites.observe(this, Observer {
            val success = it.peekContent()
            if (success) {
                removeFromFavourites.isVisible = false
                addToFavourites.isVisible = true
                toast("Тред удален из избранного")
            }
        })
        viewModel.addToFavourites.observe(this, Observer {
            val success = it.peekContent()
            if (success) {
                removeFromFavourites.isVisible = true
                addToFavourites.isVisible = false
                toast("Тред добавлен в избранное")
            }
        })

        viewModel.openPostActionDialog.observe(this, Observer {
            val view = it.peekContent()[0] as View
            val postNum = it.peekContent()[1] as String
            val dialog = PostActionDialog(this, viewModel, view, postNum)
            dialog.show()
        })
        viewModel.answerPost.observe(this, Observer {
            startActivity(
                Intent(applicationContext, MakePostActivity::class.java)
                    .putExtra(BOARD_NAME, board)
                    .putExtra(THREAD_NUM, thread)
                    .putExtra(THREAD_ANSWER, it.peekContent())
            )
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_addFavourites -> {
                viewModel.addToFavourites()
            }
            R.id.opt_removeFavourites -> {
                viewModel.removeFromFavourites()
            }
            R.id.opt_update ->{
                recyclerViewState = binding.postList.layoutManager?.onSaveInstanceState()
                viewModel.loadPosts()
            }
            R.id.opt_addPost -> {
                if (isNetworkAvailable()) startAddPostActivity() else toast(NO_INTERNET)
            }
            R.id.opt_download -> {
                if (isNetworkAvailable()) {
                    viewModel.downloadAll(applicationContext)
                    toast("Загрузка...")
                } else {
                    toast(NO_INTERNET)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.posts_menu, menu)

        addToFavourites = menu.findItem(R.id.opt_addFavourites)
        removeFromFavourites = menu.findItem(R.id.opt_removeFavourites)


        return true
    }

    private fun showContent(urls: ArrayList<String>, position: Int) {
        val urlsResult = StringBuilder()
        urls.forEach {
            urlsResult.append("${it},")
        }


        startActivity(
            Intent(applicationContext, ViewPicsActivity::class.java).putExtra(
                URLS,
                urlsResult.toString()
            ).putExtra(
                POSITION, position
            )
        )

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

//    private fun scrollToUnread() {
//        val total = postListAdapter.itemCount - 1
//        val lastReadPost = total - unreadPostsCount
//        post_list.layoutManager?.scrollToPosition(lastReadPost)
//        scrollDown.isVisible = false
//    }




}
