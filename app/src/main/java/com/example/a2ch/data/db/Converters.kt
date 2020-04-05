package com.example.a2ch.data.db

import androidx.room.TypeConverter
import com.example.a2ch.models.threads.FilesItem
import com.example.a2ch.models.threads.ThreadItem
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.util.log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Converters {
    companion object{
        @TypeConverter
        @JvmStatic
        fun jsonToFiles(value: String?): List<FilesItem> {
            val listType: Type = object : TypeToken<List<FilesItem>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun filesToJson(list: List<FilesItem>): String {
            val gson = Gson()
            return gson.toJson(list)
        }


        @TypeConverter
        @JvmStatic
        fun jsonToPosts(value: String?): List<ThreadPost> {
            val listType: Type = object : TypeToken<List<ThreadPost>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun postsToJson(list: List<ThreadPost>): String {
            val gson = Gson()
            return gson.toJson(list)
        }



    }

}