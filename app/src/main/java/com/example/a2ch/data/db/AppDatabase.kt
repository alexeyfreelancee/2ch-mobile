package com.example.a2ch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadItem
import com.example.a2ch.models.threads.ThreadPost

@Database(
    entities = [ThreadItem::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun threadDao() : ThreadDao
}