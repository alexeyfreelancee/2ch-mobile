package com.example.a2ch.ui.category_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.R
import com.example.a2ch.databinding.ActivityCategoriesBinding
import com.example.a2ch.ui.category.CategoryActivity
import com.example.a2ch.util.BOARD_NAME
import com.example.a2ch.util.CATEGORY_NAME
import com.example.a2ch.util.log
import kotlinx.android.synthetic.main.activity_categories.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class CategoriesActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: CategoriesViewModelFactory by instance()
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var categoriesListAdapter: CategoriesListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(CategoriesViewModel::class.java)
        categoriesListAdapter = CategoriesListAdapter(viewModel)
        intent.getStringExtra(BOARD_NAME)?.let {
            viewModel.categoryName = it
            log(it)
        }
        log("created")
        DataBindingUtil.setContentView<ActivityCategoriesBinding>(
            this,
            R.layout.activity_categories
        ).apply {
            viewmodel = viewModel
            lifecycleOwner = this@CategoriesActivity
        }

        initList()
        initObservers()
    }

    private fun initObservers() {
        viewModel.categories.observe(this, Observer {
            categoriesListAdapter.updateList(it)
        })

        viewModel.startActivity.observe(this, Observer {
            val name = it.peekContent()

            startActivity(
                Intent(applicationContext, CategoryActivity::class.java).putExtra(
                    CATEGORY_NAME, name
                )
            )
        })
    }

    private fun initList() {
        categories_list.adapter = categoriesListAdapter
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.loadCategories()
    }
}
