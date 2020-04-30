package com.example.a2ch.ui.threads

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a2ch.R
import com.example.a2ch.adapters.ThreadListAdapter
import com.example.a2ch.databinding.ActivityCategoryBinding
import com.example.a2ch.ui.make_post.MakePostActivity
import com.example.a2ch.ui.posts.PostsActivity
import com.example.a2ch.util.BOARD_NAME
import com.example.a2ch.util.THREAD_NUM
import com.example.a2ch.util.initError
import com.example.a2ch.util.log
import kotlinx.android.synthetic.main.activity_category.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ThreadsActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: CategoryViewModelFactory by instance()
    private lateinit var threadsListAdapter: ThreadListAdapter
    private lateinit var viewModel: ThreadsViewModel
    private var boardName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(ThreadsViewModel::class.java)
        intent.getStringExtra(BOARD_NAME)?.let {
            boardName = it
        }
        viewModel.setBoardName(boardName)
        DataBindingUtil.setContentView<ActivityCategoryBinding>(
            this,
            R.layout.activity_category
        ).apply {
            viewmodel = viewModel
            lifecycleOwner = this@ThreadsActivity
        }

        initThreadList()
        initObservers()
    }


    private fun initObservers() {
        viewModel.category.observe(this, Observer {
            supportActionBar?.title = it.boardName
        })

        viewModel.startPostsActivity.observe(this, Observer {
            val threadNum = it.peekContent()
           startPostsActivity(threadNum)
        })

        viewModel.error.observe(this, Observer {
            initError(this, it)
        })
    }

    private fun startPostsActivity(thread: String){
        startActivity(
            Intent(applicationContext, PostsActivity::class.java)
                .putExtra(THREAD_NUM, thread)
                .putExtra(BOARD_NAME, boardName)
        )
    }

    private fun initThreadList() {
        viewModel.threads.observe(this, Observer {
            threadsListAdapter.submitList(it)
            thread_list.scheduleLayoutAnimation()
        })
        threadsListAdapter = ThreadListAdapter(viewModel)
        thread_list.adapter = threadsListAdapter
    }
}
