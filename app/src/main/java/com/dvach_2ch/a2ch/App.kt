package com.dvach_2ch.a2ch

import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.dvach_2ch.a2ch.data.Repository
import com.dvach_2ch.a2ch.data.SharedPrefsHelper
import com.dvach_2ch.a2ch.data.db.AppDatabase
import com.dvach_2ch.a2ch.data.networking.RetrofitClient
import com.dvach_2ch.a2ch.ui.boards.BoardsViewModelFactory
import com.dvach_2ch.a2ch.ui.favourite.FavouritesViewModelFactory
import com.dvach_2ch.a2ch.ui.gallery.GalleryViewModelFactory

import com.dvach_2ch.a2ch.ui.history.HistoryViewModelFactory
import com.dvach_2ch.a2ch.ui.make_post.SendPostViewModelFactory
import com.dvach_2ch.a2ch.ui.posts.PostsViewModelFactory
import com.dvach_2ch.a2ch.ui.threads.CategoryViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : MultiDexApplication(), KodeinAware {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object{
        var context: Context? =null
        fun provideContext() = context
    }

    override val kodein = Kodein.lazy {
        bind() from singleton { RetrofitClient }
        bind() from  eagerSingleton {
            Room.databaseBuilder(this@App, AppDatabase::class.java, "dvach-db")
                .fallbackToDestructiveMigration()
                .build()
        }

        bind() from singleton { SharedPrefsHelper(this@App) }
        bind() from singleton { Repository(instance(), instance(), instance()) }
        bind() from singleton { HistoryViewModelFactory(instance()) }
        bind() from singleton { FavouritesViewModelFactory(instance()) }
        bind() from singleton { BoardsViewModelFactory(instance()) }
        bind() from singleton { CategoryViewModelFactory(instance()) }
        bind() from singleton { PostsViewModelFactory(instance()) }
        bind() from singleton { SendPostViewModelFactory(instance()) }
        bind() from singleton { GalleryViewModelFactory(instance()) }
    }
}