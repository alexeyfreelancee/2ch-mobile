package com.example.a2ch.data.db
import android.provider.SyncStateContract.Helpers.insert
import androidx.room.*
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadItem

@Dao
interface ThreadDao {

    @Query("SELECT * FROM thread_table WHERE isFavourite = 1 ORDER BY timestamp DESC")
    suspend fun getFavouriteThreads(): List<ThreadItem>

    @Query("SELECT * FROM thread_table ORDER BY timestamp DESC")
    suspend fun getHistoryThreads(): List<ThreadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveThread(threadItem: ThreadItem)

    suspend fun insertWithTimestamp(data: ThreadItem) {
        saveThread(data.apply{
            timestamp = System.currentTimeMillis()
        })
    }
    @Query("select * from thread_table where board like :board and threadNum like :threadNum")
    suspend fun getThread(board: String, threadNum: String) : ThreadItem?



}