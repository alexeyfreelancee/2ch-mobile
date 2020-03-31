package com.example.a2ch.models.post

import com.google.gson.annotations.SerializedName

class MakePostError(
    @SerializedName("Error")
    val error: String?,
    @SerializedName("Reason")
    val reason: String?
)