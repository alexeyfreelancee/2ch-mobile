package com.alexey_vena.a2ch.models.captcha

import com.google.gson.annotations.SerializedName

data class VerifyCaptcha(@SerializedName("hostname")
                         val hostname: String = "",
                         @SerializedName("success")
                         val success: Boolean = false,
                         @SerializedName("challenge_ts")
                         val challengeTs: String = "")