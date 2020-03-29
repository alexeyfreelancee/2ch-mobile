package com.example.a2ch.ui.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.R
import com.example.a2ch.databinding.ActivityCategoryBinding
import com.example.a2ch.util.CATEGORY_NAME
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class CategoryActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: CategoryViewModelFactory by instance()
    private val threadsListAdapter = ThreadsListAdapter()
    private lateinit var viewModel: CategoryViewModel
    private var categoryName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(CategoryViewModel::class.java)
        intent.getStringExtra(CATEGORY_NAME)?.let {
            categoryName = it
            viewModel.categoryName = it
        }

       val binding = DataBindingUtil.setContentView<ActivityCategoryBinding>(this, R.layout.activity_category)
            .apply {
                viewmodel = viewModel
                lifecycleOwner = this@CategoryActivity
            }
        setSupportActionBar(binding.appBarLayout.toolbar)

        initToolbar()
        initThreadList()
        initObservers()
    }

    private fun initToolbar(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initObservers() {
        viewModel.threads.observe(this, Observer {
            threadsListAdapter.updateList(it)
        })
        viewModel.category.observe(this, Observer {
            supportActionBar?.title = it.boardName
        })
    }

    private fun initThreadList() {
        thread_list.adapter = threadsListAdapter
    }
}
