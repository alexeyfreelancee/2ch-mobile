package com.alexey_vena.a2ch.ui.favourite

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.alexey_vena.a2ch.data.Repository
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.threads.ThreadsDataSource
import com.alexey_vena.a2ch.util.log
import kotlinx.coroutines.runBlocking

class FavouritesDataSource(private val repository: Repository,private val empty:MutableLiveData<Boolean>) : PositionalDataSource<ThreadPost>(){


    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ThreadPost>) {

        runBlocking {
            val threads =   repository.loadFavourites()

            empty.postValue(threads.isEmpty())
            val endPosition = if(params.loadSize > threads.size) threads.size  else params.startPosition + params.loadSize

            if (endPosition <= threads.size) {

                callback.onResult(
                    threads.subList(
                        params.startPosition,
                        endPosition
                    )
                )
            }
        }

    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ThreadPost>) {

        runBlocking {
            val threads =   repository.loadFavourites()
            empty.postValue(threads.isEmpty())
            val endPosition = if(params.requestedLoadSize > threads.size) threads.size  else params.requestedStartPosition + params.requestedLoadSize
            if (endPosition <= threads.size) {
                callback.onResult(
                    threads.subList(
                        params.requestedStartPosition,
                        endPosition
                    ), 0, threads.size
                )
            }

        }
    }

}


class FavouritesDataSourceFactory(private val repository: Repository, private val empty: MutableLiveData<Boolean>) :
    DataSource.Factory<Int, ThreadPost>() {
    override fun create(): DataSource<Int, ThreadPost> {
        return FavouritesDataSource(
            repository,empty
        )
    }
}