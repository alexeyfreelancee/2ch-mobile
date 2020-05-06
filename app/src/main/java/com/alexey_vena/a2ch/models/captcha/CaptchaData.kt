package com.alexey_vena.a2ch.models.captcha

import com.google.gson.annotations.SerializedName

data class CaptchaData(@SerializedName("result")
                   val result: Int = 0,
                       @SerializedName("id")
                   val id: String = "",
                       @SerializedName("type")
                   val type: String = "",
                       var url: String = "")