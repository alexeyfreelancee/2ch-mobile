package com.dvach_2ch.a2ch.ui.media_slider

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.util.POSITION
import com.dvach_2ch.a2ch.util.URLS
import com.dvach_2ch.a2ch.views.ViewPagerFixed
import com.dvach_2ch.a2ch.util.toast
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrPosition
import kotlinx.android.synthetic.main.view_content_activity.view.*
import java.io.File


class MediaSliderActivity : AppCompatActivity() {
    private var mediaSliderAdapter: MediaSliderAdapter? = null
    private var urls = ArrayList<String>()
    private var position: Int = 0
    private lateinit var contentSlider: ViewPagerFixed

    private var positionView :TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_content_activity)

        getData(intent)


        positionView = findViewById(R.id.tv_position)
        positionView?.text = "${position + 1}/${urls.size}"

        setupSlidr()
        setupContentSlider()
        setupDownloadButton()
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

            val manager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            applicationContext.toast("Загрузка...")
        } catch (ex: Exception) {
            applicationContext.toast("Ошибка :(")
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

    private fun getData(arguments: Intent?) {
        val urlArray = arguments?.getStringExtra(URLS)?.split(",")

        urlArray?.forEach {
            if (it.length > 5) urls.add(it)
        }

        position = arguments?.getIntExtra(POSITION, 0) ?: 0

    }

    private fun setupContentSlider(){
        mediaSliderAdapter =
            MediaSliderAdapter(
                applicationContext,
                urls
            )
        contentSlider = findViewById(R.id.photo)
        contentSlider.apply {
            adapter = mediaSliderAdapter
            currentItem = position
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    positionView?.text = "${position + 1}/${urls.size}"
                    mediaSliderAdapter!!.pausePlayers()
                }


                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }
            })
        }
    }

    private fun setupDownloadButton(){
        val download = findViewById<ImageView>(R.id.download)
        download!!.setOnClickListener {
            download(urls[contentSlider.currentItem])
        }
    }

    private fun setupSlidr(){
        val config = SlidrConfig.Builder()
            .position(SlidrPosition.BOTTOM)
            .sensitivity(0.05f)
            .distanceThreshold(0.01f)
            .build()
        Slidr.attach(this, config)
    }

    override fun onPause() {
        mediaSliderAdapter!!.pausePlayers()
        super.onPause()
    }

    override fun onDestroy() {
        mediaSliderAdapter!!.releasePlayers()

        super.onDestroy()
    }
}