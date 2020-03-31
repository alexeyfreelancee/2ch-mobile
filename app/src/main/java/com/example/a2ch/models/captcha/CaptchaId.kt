package com.example.a2ch.models.captcha

import com.google.gson.annotations.SerializedName

data class CaptchaId(@SerializedName("result")
                   val result: Int = 0,
                     @SerializedName("id")
                   val id: String = "",
                     @SerializedName("type")
                   val type: String = "")