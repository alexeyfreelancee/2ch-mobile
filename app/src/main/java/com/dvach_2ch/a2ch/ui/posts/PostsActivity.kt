package com.dvach_2ch.a2ch.ui.posts

import android.content.Intent
import android.content.res.Resources
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
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.adapters.PostListAdapter
import com.dvach_2ch.a2ch.databinding.ActivityPostsBinding
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.ui.gallery.GalleryActivity
import com.dvach_2ch.a2ch.ui.make_post.MakePostActivity
import com.dvach_2ch.a2ch.ui.media_slider.MediaSliderActivity
import com.dvach_2ch.a2ch.ui.posts.dialogs.AnswersDialog
import com.dvach_2ch.a2ch.ui.posts.dialogs.PostActionDialog
import com.dvach_2ch.a2ch.ui.posts.dialogs.ViewPostDialog
import com.dvach_2ch.a2ch.util.*
import com.dvach_2ch.a2ch.views.RecyclerFastScroll
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

    private var addToFavourites: MenuItem? = null
    private var removeFromFavourites: MenuItem? = null

    private var recyclerViewState: Parcelable? = null

    private lateinit var binding: ActivityPostsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(PostsViewModel::class.java)
        initObservers()
        binding =
            DataBindingUtil.setContentView<ActivityPostsBinding>(this, R.layout.activity_posts)
                .apply {
                    viewmodel = viewModel
                    lifecycleOwner = this@PostsActivity
                }



        initViewModelData()
        initPostList()
    }


    private fun initViewModelData() {
        board = intent.getStringExtra(BOARD_NAME)
        thread = intent.getStringExtra(THREAD_NUM)
        viewModel.apply {
            board = this@PostsActivity.board
            threadNum = this@PostsActivity.thread
        }


    }

    override fun onResume() {
        viewModel.loadPosts()
        super.onResume()
    }

    private fun initObservers() {

        viewModel.contentDialogData.observe(this, Observer {
            if (!it.hasBeenHandled) {
                val data = it.peekContent()
                showContent(data.urls, data.position)
            }

        })
        viewModel.error.observe(this, Observer {
            if (!it.hasBeenHandled) initError(this, it.peekContent())
        })
        viewModel.openPostDialog.observe(this, Observer {
            if (!it.hasBeenHandled) openPostDialog(it.peekContent())

        })
        viewModel.showAnswers.observe(this, Observer {
            if (!it.hasBeenHandled) openAnswersDialog(it.peekContent())
        })
        viewModel.openWebLink.observe(this, Observer {
            if (!it.hasBeenHandled) {
                val url = it.peekContent()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

        })

        viewModel.isFavourite.observe(this, Observer {
            if (it) {
                removeFromFavourites?.isVisible = true
                addToFavourites?.isVisible = false
            } else {
                removeFromFavourites?.isVisible = false
                addToFavourites?.isVisible = true
            }
        })
        viewModel.removeFromFavourites.observe(this, Observer {
            if (!it.hasBeenHandled) {
                val success = it.peekContent()
                if (success) {
                    removeFromFavourites?.isVisible = false
                    addToFavourites?.isVisible = true
                    toast("Тред удален из избранного")
                }
            }

        })
        viewModel.addToFavourites.observe(this, Observer {
            if (!it.hasBeenHandled) {
                val success = it.peekContent()
                if (success) {
                    removeFromFavourites?.isVisible = true
                    addToFavourites?.isVisible = false
                    toast("Тред добавлен в избранное")
                }
            }

        })

        viewModel.openPostActionDialog.observe(this, Observer {
            if (!it.hasBeenHandled) {
                val view = it.peekContent()[0] as View
                val postNum = it.peekContent()[1] as String
                val dialog = PostActionDialog(this, viewModel, view, postNum)
                dialog.show()
            }

        })
        viewModel.answerPost.observe(this, Observer {
            if (!it.hasBeenHandled) {
                startActivity(
                    Intent(applicationContext, MakePostActivity::class.java)
                        .putExtra(BOARD_NAME, board)
                        .putExtra(THREAD_NUM, thread)
                        .putExtra(THREAD_ANSWER, it.peekContent())
                )
            }

        })

    }

    private fun initPostList() {
        postListAdapter = PostListAdapter(viewModel)
        binding.postList.adapter = postListAdapter
        viewModel.posts.observe(this, Observer {
            postListAdapter.updateList(it)
            if (recyclerViewState != null) {
                binding.postList.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        })
        RecyclerFastScroll(binding.postList, resources.getColor(R.color.colorAccent), resources.getColor(R.color.colorAccent))
        recyclerViewState = binding.postList.layoutManager?.onSaveInstanceState()
    }

    private fun openAnswersDialog(answers: List<ThreadPost>) {
        val dialog = AnswersDialog(answers, viewModel, this)
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_addFavourites -> {
                viewModel.addToFavourites()
            }
            R.id.gallery -> {
                startActivity(Intent(this, GalleryActivity::class.java).apply {
                    putExtra(THREAD_NUM, thread)
                    putExtra(BOARD_NAME, board)
                })
            }
            R.id.copyUrl -> {
                viewModel.copyThreadUrl(this)
            }
            R.id.opt_removeFavourites -> {
                viewModel.removeFromFavourites()
            }
            R.id.opt_update -> {
                recyclerViewState = binding.postList.layoutManager?.onSaveInstanceState()
                viewModel.needToShowProgress = true
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


        viewModel.checkFavourite()
        return true
    }

    private fun showContent(urls: ArrayList<String>, position: Int) {
        val urlsResult = StringBuilder()
        urls.forEach {
            urlsResult.append("${it},")
        }


        startActivity(
            Intent(applicationContext, MediaSliderActivity::class.java).putExtra(
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

    override fun onStart() {
        super.onStart()
        supportActionBar?.title = ""
    }

    override fun onStop() {
        super.onStop()
        supportActionBar?.title = ""
    }
//    private fun scrollToUnread() {
//        val total = postListAdapter.itemCount - 1
//        val lastReadPost = total - unreadPostsCount
//        post_list.layoutManager?.scrollToPosition(lastReadPost)
//        scrollDown.isVisible = false
//    }


}
