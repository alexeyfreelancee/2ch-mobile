package com.dvach_2ch.a2ch.data.db
import androidx.room.*
import com.dvach_2ch.a2ch.models.threads.ThreadItem

@Dao
interface ThreadDao {

    @Query("SELECT * FROM thread_table WHERE isFavourite = 1 ORDER BY saveTime DESC")
    suspend fun getFavouriteThreads(): List<ThreadItem>

    @Query("SELECT * FROM thread_table ORDER BY saveTime DESC")
    suspend fun getHistoryThreads(): List<ThreadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveThread(threadItem: ThreadItem)


    suspend fun saveWithTimestamp(data: ThreadItem) {
        saveThread(data.apply{
            saveTime = System.currentTimeMillis()
        })
    }

    @Update
    suspend fun updateThread(thread: ThreadItem)

    @Query("select * from thread_table where board like :board and num like :threadNum")
    suspend fun getThread(board: String, threadNum: String) : ThreadItem?



}