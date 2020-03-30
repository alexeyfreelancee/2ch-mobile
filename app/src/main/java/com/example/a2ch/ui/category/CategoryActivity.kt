package com.example.a2ch.ui.category

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a2ch.R
import com.example.a2ch.adapters.ThreadListAdapter
import com.example.a2ch.databinding.ActivityCategoryBinding
import com.example.a2ch.ui.posts.PostsActivity
import com.example.a2ch.util.CATEGORY_NAME
import com.example.a2ch.util.THREAD_NUM
import com.example.a2ch.util.log
import kotlinx.android.synthetic.main.activity_category.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class CategoryActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: CategoryViewModelFactory by instance()
    private lateinit var threadsListAdapter: ThreadListAdapter
    private lateinit var viewModel: CategoryViewModel
    private var categoryName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(CategoryViewModel::class.java)
        threadsListAdapter = ThreadListAdapter(viewModel)

        intent.getStringExtra(CATEGORY_NAME)?.let {
            categoryName = it
            viewModel.categoryName = it
            log("$categoryName category")
        }
        viewModel.update()

       DataBindingUtil.setContentView<ActivityCategoryBinding>(
            this,
            R.layout.activity_category
        ).apply {
            viewmodel = viewModel
            lifecycleOwner = this@CategoryActivity
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
                .putExtra(CATEGORY_NAME, categoryName))
        })

    }

    private fun initThreadList() {
        val linearLayoutManager = object: LinearLayoutManager(this){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        thread_list.apply {
            adapter = threadsListAdapter
            layoutManager =linearLayoutManager
        }
    }
}
