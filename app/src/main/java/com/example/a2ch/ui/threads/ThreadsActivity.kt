package com.example.a2ch.ui.threads

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.example.a2ch.util.toast
import kotlinx.android.synthetic.main.activity_category.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ThreadsActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: CategoryViewModelFactory by instance()
    private lateinit var threadsListAdapter: ThreadListAdapter
    private lateinit var viewModel: CategoryViewModel
    private var boardName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(CategoryViewModel::class.java)
        threadsListAdapter = ThreadListAdapter(viewModel)

        intent.getStringExtra(BOARD_NAME)?.let {
            boardName = it
            viewModel.boardName = it
        }
        viewModel.update()


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
        viewModel.threads.observe(this, Observer {
            threadsListAdapter.updateList(it)

        })

        viewModel.category.observe(this, Observer {
            supportActionBar?.title = it.boardName
        })

        viewModel.startPostsActivity.observe(this, Observer {
            startActivity(
                Intent(applicationContext, PostsActivity::class.java)
                    .putExtra(THREAD_NUM, it.peekContent())
                    .putExtra(BOARD_NAME, boardName)
            )
        })

        viewModel.error.observe(this, Observer {
            initError(this, it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.threads_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.opt_add) startAddThreadActivity()
        return super.onOptionsItemSelected(item)
    }

    private fun startAddThreadActivity() {
        startActivity(
            Intent(applicationContext, MakePostActivity::class.java)
                .putExtra(BOARD_NAME, boardName)
                .putExtra(THREAD_NUM, "0")
        )
    }

    private fun initThreadList() {
        val linearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        linearLayoutManager.isAutoMeasureEnabled = true
        thread_list.apply {
            adapter = threadsListAdapter
            layoutManager = linearLayoutManager
        }
    }
}
