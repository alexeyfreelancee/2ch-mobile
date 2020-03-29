package com.example.a2ch

import android.app.Application
import com.example.a2ch.data.Repository
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.ui.category.CategoryViewModelFactory
import com.example.a2ch.ui.category_list.CategoriesViewModel
import com.example.a2ch.ui.category_list.CategoriesViewModelFactory
import com.example.a2ch.ui.thread_list.ThreadsViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : Application(),KodeinAware{

    override val kodein = Kodein.lazy {
        bind() from singleton { RetrofitClient }
        bind() from singleton { Repository(instance()) }

        bind() from singleton { ThreadsViewModelFactory(instance()) }
        bind() from singleton { CategoriesViewModelFactory(instance()) }
        bind() from singleton { CategoryViewModelFactory(instance()) }
    }
}