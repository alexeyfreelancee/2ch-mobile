package com.example.a2ch.data

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.util.log
import kotlinx.coroutines.runBlocking

class ThreadsDataSource(private val repository: Repository, private val board: String) :
    PositionalDataSource<ThreadPost>() {
    private var threadList: ArrayList<ThreadPost>? = null

    private suspend fun loadThreads(): ArrayList<ThreadPost> {

        val threadBase = repository.loadBoardInfo(board)
        val threadList = ArrayList<ThreadPost>()
        threadBase.threadItems.forEach { thread ->
            threadList.add(
                ThreadPost(
                    comment = thread.comment,
                    subject = thread.name,
                    timestamp = thread.loadTime,
                    postsCount = thread.postsCount,
                    files = thread.files,
                    num = thread.num
                )
            )
        }
        this.threadList = threadList
        return threadList
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ThreadPost>) {
        runBlocking {
            val threads = threadList ?: loadThreads()
            val endPosition =  params.startPosition + params.loadSize
            log("start position ${params.startPosition}, load size: ${params.loadSize}")
            if(endPosition<= threads.size){
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
            val threads = threadList ?: loadThreads()
            val endPosition = params.requestedStartPosition + params.requestedLoadSize
            log(" requestedStartPosition ${params.requestedStartPosition},  requestedLoadSize: ${params.requestedLoadSize}")
            if(endPosition<= threads.size){
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

class ThreadDataSourceFactory(private val repository: Repository, private val board: String) :
    DataSource.Factory<Int, ThreadPost>() {
    override fun create(): DataSource<Int, ThreadPost> {
        return ThreadsDataSource(repository, board)
    }
}