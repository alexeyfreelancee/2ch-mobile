package com.example.a2ch.util

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.Error
import com.example.a2ch.ui.posts.PostsViewModel
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


val myOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .centerCrop()

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun log(msg: Any){
    when(msg){
        is String ->Log.d("TAGG", msg)
        else -> Log.d("TAGG", msg.toString())
    }

}
val greenColorList = ColorStateList(
    arrayOf(
        intArrayOf(R.attr.state_pressed),
        intArrayOf(R.attr.state_focused),
        intArrayOf(R.attr.state_focused, R.attr.state_pressed)
    ), intArrayOf(
        Color.GREEN,
        Color.GREEN,
        Color.GREEN
    )
)
fun String.parseDigits(): String{
    return this.replace("\\D+".toRegex(),"");
}
fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
fun getDate(unix: Long) : String{
    val date = Date(unix * 1000)
    val sdf = SimpleDateFormat("dd.MM hh:mm", Locale.getDefault())
    return sdf.format(date)
}

fun provideCaptchaUrl(id: String) : String{
    return "/api/captcha/2chaptcha/image/$id"
}


fun String.isWebLink() : Boolean{
    return try {
        val url = java.net.URL(this)
        true
    } catch (ex: Exception){
        false
    }

}


fun initError(activity: Activity,event: Event<Error>){
    val error = event.peekContent()
    activity.toast(error.msg)
    if(error.type == CRITICAL) activity.finish()
}

@BindingAdapter("html")
fun displayHtml(view: HtmlTextView, html: String?) {
    if(html!=null){
        view.text = HtmlFormatter.formatHtml(HtmlFormatterBuilder()
            .setHtml(html))

    }

}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if(url!=null){
        Glide.with(view.context)
            .load("https://2ch.hk${url}")
            .apply(myOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}


fun setTextViewHTML(text: TextView, html: String?, viewModel: PostsViewModel) {
    val sequence = Html.fromHtml(html)
    val strBuilder = SpannableStringBuilder(sequence)

    makeRepliesGreen(strBuilder)
    makeLinkClickable(sequence,strBuilder, viewModel)

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
){
    val color = Color.rgb(120,153,34)
    val lines = stringBuilder.split("\\r?\\n".toRegex());
    lines.forEach {
        if(it.startsWith(">")){
            if(!it.startsWith(">>")){
                val start = stringBuilder.indexOf(it)
                val end = start + it.length

                stringBuilder.setSpan(
                    ForegroundColorSpan(color),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

            }

        }
    }


}



