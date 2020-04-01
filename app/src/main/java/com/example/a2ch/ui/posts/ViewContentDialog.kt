package com.example.a2ch.ui.posts

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.example.a2ch.R
import com.example.a2ch.util.gone
import com.example.a2ch.util.toast
import com.example.a2ch.util.visible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import java.io.File

class ViewContentDialog(private val ctx: Context, private val url: String) : Dialog(ctx) {
    private var player: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.dialog_photo)

        val image = findViewById<ImageView>(R.id.photo)
        val download = findViewById<ImageView>(R.id.download)
        val video = findViewById<PlayerView>(R.id.video)
        val progressBar = findViewById<ProgressBar>(R.id.progress)

        if (url.endsWith("mp4") || url.endsWith("webm")) {
            image.gone()
            video.visible()
            progressBar.visible()
            setupVideo(video, download, progressBar)
        } else {
            image.visible()
            video.gone()
            progressBar.gone()
            setupImage(image, download)
        }
    }

    private fun setupImage(image: ImageView, download: ImageView) {
        Glide.with(ctx)
            .load(url)
            .into(image)

        download.setOnClickListener {
            download(url, ".jpg")
        }
    }

    private fun setupVideo(
        video: PlayerView,
        download: ImageView,
        progressBar: ProgressBar
    ) {
        player = ExoPlayerFactory.newSimpleInstance(ctx)
        video.player = player

        player?.apply {
            prepare(createMediaSource())
            playWhenReady = true
            addListener(object : Player.DefaultEventListener() {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                    if (playbackState == ExoPlayer.STATE_BUFFERING) progressBar.visible() else progressBar.gone()
                    if (playbackState == ExoPlayer.STATE_IDLE) player?.prepare(createMediaSource())
                }
            })
        }


        download.setOnClickListener {
            download(url, ".mp4")
        }
    }

    private fun createMediaSource(): ExtractorMediaSource {
        return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory("2ch"))
            .createMediaSource(Uri.parse(url))
    }


    private fun download(url: String, prefix: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
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
        } catch (ex: Exception){
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player?.apply {
            release()
            playWhenReady = false;
            stop();
            seekTo(0);
        }
    }

}