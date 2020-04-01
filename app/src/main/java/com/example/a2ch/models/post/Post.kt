package com.example.a2ch.models.post

import com.google.gson.annotations.SerializedName

data class Post(@SerializedName("date")
                var date: String = "",
                @SerializedName("op")
                val op: Int = 0,
                @SerializedName("parent")
                val parent: String = "",
                @SerializedName("subject")
                val subject: String = "",
                @SerializedName("num")
                val num: String = "",
                @SerializedName("endless")
                val endless: Int = 0,
                @SerializedName("lasthit")
                val lasthit: Int = 0,
                @SerializedName("unique_posters")
                val uniquePosters: String = "",
                @SerializedName("trip_type")
                val tripType: String = "",
                @SerializedName("trip")
                val trip: String = "",
                @SerializedName("name")
                var name: String = "",
                @SerializedName("sticky")
                val sticky: Int = 0,
                @SerializedName("closed")
                val closed: Int = 0,
                @SerializedName("comment")
                var comment: String = "",
                @SerializedName("banned")
                val banned: Int = 0,
                @SerializedName("email")
                val email: String = "",
                @SerializedName("timestamp")
                val timestamp: Long = 0,
                @SerializedName("files")
                val files: List<FilePost>)