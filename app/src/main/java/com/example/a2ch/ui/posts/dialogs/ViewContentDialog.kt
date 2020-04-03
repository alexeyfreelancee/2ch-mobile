package com.example.a2ch.ui.posts.dialogs

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import com.example.a2ch.R
import com.example.a2ch.adapters.ContentSliderAdapter
import com.example.a2ch.util.log
import com.example.a2ch.util.toast
import java.io.File

class ViewContentDialog(
    private val ctx: Context,
    private val urls: ArrayList<String>,
    private val position: Int
) : Dialog(ctx) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.dialog_photo)

        val contentSlider = findViewById<ViewPager>(R.id.photo).apply {
            offscreenPageLimit = 0
            adapter =  ContentSliderAdapter(ctx, urls)
            currentItem = position
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    log("page selected $position")
                }
            })
        }

        val download = findViewById<ImageView>(R.id.download)
        download.setOnClickListener {
            download(urls[contentSlider.currentItem])
        }

    }


    private fun download(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            val prefix = if (url.endsWith(".mp4") || url.endsWith("webm")) ".mp4" else ".jpg"
            val file = createFile(prefix)
            val title = if (prefix == ".mp4") "video" else "photo"


            request.apply {
                setTitle("2ch $title")
                setDescription(url)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationUri(Uri.fromFile(file))
            }

            val manager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            ctx.toast("Загрузка...")
        } catch (ex: Exception) {
            ctx.toast("Ошибка :(")
        }

    }


    private fun createFile(prefix: String): File {
        val root: String = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/2ch")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val name = System.currentTimeMillis().toString() + prefix
        return File(myDir, name)
    }
}