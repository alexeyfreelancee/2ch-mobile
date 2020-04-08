package com.example.a2ch

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.example.a2ch.data.Repository
import com.example.a2ch.data.db.AppDatabase
import com.example.a2ch.data.networking.RetrofitClient
import com.example.a2ch.ui.boards.BoardsViewModelFactory
import com.example.a2ch.ui.favourite.FavouritesViewModelFactory
import com.example.a2ch.ui.history.HistoryViewModelFactory
import com.example.a2ch.ui.make_post.SendPostViewModelFactory
import com.example.a2ch.ui.threads.CategoryViewModelFactory

import com.example.a2ch.ui.posts.PostsViewModelFactory

import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : MultiDexApplication(), KodeinAware {

    override val kodein = Kodein.lazy {
        bind() from singleton { RetrofitClient }
        bind() from  eagerSingleton {
            Room.databaseBuilder(this@App, AppDatabase::class.java, "dvach-db")
                .build()
        }

        bind() from singleton { Repository(instance(), instance()) }

        bind() from singleton { HistoryViewModelFactory(instance()) }
        bind() from singleton { FavouritesViewModelFactory(instance()) }
        bind() from singleton { BoardsViewModelFactory(instance()) }
        bind() from singleton { CategoryViewModelFactory(instance()) }
        bind() from singleton { PostsViewModelFactory(instance()) }
        bind() from singleton { SendPostViewModelFactory(instance()) }
    }
}