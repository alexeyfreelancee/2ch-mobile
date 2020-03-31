package com.example.a2ch.ui.threads

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a2ch.R
import com.example.a2ch.adapters.ThreadListAdapter
import com.example.a2ch.databinding.ActivityCategoryBinding
import com.example.a2ch.ui.posts.PostsActivity
import com.example.a2ch.util.BOARD_NAME
import com.example.a2ch.util.THREAD_NUM
import com.example.a2ch.util.log
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
    private var categoryName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(CategoryViewModel::class.java)
        threadsListAdapter = ThreadListAdapter(viewModel)

        intent.getStringExtra(BOARD_NAME)?.let {
            categoryName = it
            viewModel.categoryName = it
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
            log(it.size.toString())
        })

        viewModel.category.observe(this, Observer {
            supportActionBar?.title = it.boardName
        })

        viewModel.startActivity.observe(this, Observer {
            startActivity(Intent(applicationContext, PostsActivity::class.java)
                .putExtra(THREAD_NUM, it.peekContent())
                .putExtra(BOARD_NAME, categoryName))
        })

        viewModel.error.observe(this, Observer {
            toast(it)
            finish()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_option, menu)

        val searchItem = menu?.findItem(R.id.opt_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                threadsListAdapter.filter.filter(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    private fun initThreadList() {
        val linearLayoutManager = object: LinearLayoutManager(this){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        linearLayoutManager.isAutoMeasureEnabled = true
        thread_list.apply {
            adapter = threadsListAdapter
            layoutManager =linearLayoutManager
        }
    }
}
