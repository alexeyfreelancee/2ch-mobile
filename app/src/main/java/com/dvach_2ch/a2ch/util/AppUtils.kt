package com.dvach_2ch.a2ch.util


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.dvach_2ch.a2ch.App
import com.dvach_2ch.a2ch.models.util.CRITICAL
import com.dvach_2ch.a2ch.models.util.Error
import com.dvach_2ch.a2ch.ui.posts.PostsViewModel
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("answers")
fun setAnswers(view:TextView, answers:Int?){
    if(answers!=null){
        val root = "ответ"
        val endings = arrayOf("a", "ов")
        val number = answers % 100


        val result = if(number in 11..19){
            root + endings[1]
        } else{
            when(number % 10){
                1 -> root
                2 -> root + endings[0]
                3 -> root + endings[0]
                4 -> root + endings[0]
                else ->  root + endings[1]
            }
        }
        view.text = "$answers $result"
    }
}
val myOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .centerCrop()

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun log(vararg message: Any?) {
    message.forEach { msg ->
        when (msg) {
            is String -> Log.d("TAGG", msg)
            else -> Log.d("TAGG", msg.toString())
        }
    }


}


fun String.parseDigits(): String {
    return this.replace("\\D+".toRegex(), "");
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun getDate(unix: Long): String {
    val date = Date(unix * 1000)
    val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    return sdf.format(date)
}

fun provideCaptchaUrl(id: String): String {
    return "/api/captcha/2chaptcha/image/$id"
}


fun String.isWebLink(): Boolean {
    return try {
        java.net.URL(this)
        true
    } catch (ex: Exception) {
        false
    }

}


fun initError(activity: Activity, error:Error) {

    activity.toast(error.msg)
    if (error.type == CRITICAL) activity.finish()
}

@BindingAdapter("html")
fun displayHtml(view: HtmlTextView, html: String?) {
    if (html != null) {
        view.text = HtmlFormatter.formatHtml(HtmlFormatterBuilder().setHtml(html))
    }

}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context)
            .load("https://2ch.hk${url}")
            .apply(myOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}


@BindingAdapter("date")
fun setDate(view: TextView, timestamp: Long) {
    view.text = getDate(timestamp)
}

fun parseThreadDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    return sdf.format(date)
}

fun isNetworkAvailable(): Boolean {
    val context = App.provideContext()
    if (context != null) {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    } else {
        val runtime = Runtime.getRuntime()
        return try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            exitValue == 0
        } catch (ex: Exception) {
            false
        }
    }

}


fun setTextViewHTML(text: TextView, html: String?, viewModel: PostsViewModel) {
    val sequence = Html.fromHtml(html)
    val strBuilder = SpannableStringBuilder(sequence)

    makeRepliesGreen(strBuilder)
    makeLinkClickable(sequence, strBuilder, viewModel)

    text.text = strBuilder
    text.movementMethod = LinkMovementMethod.getInstance()
}

private fun makeLinkClickable(
    sequence: Spanned,
    strBuilder: SpannableStringBuilder,
    viewModel: PostsViewModel
) {
    val urls =
        strBuilder.getSpans(0, sequence.length, URLSpan::class.java)

    for (span in urls) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (span != null) viewModel.openUrl(span.url)
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

}


private fun makeRepliesGreen(
    stringBuilder: SpannableStringBuilder
) {
    val color = Color.rgb(120, 153, 34)
    val lines = stringBuilder.split("\\r?\\n".toRegex())
    lines.forEach {
        if (it.contains(">") && !it.contains(">>")) {
            val start = stringBuilder.indexOf(it)
            val end = start + it.length

            stringBuilder.setSpan(
                ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }


}



