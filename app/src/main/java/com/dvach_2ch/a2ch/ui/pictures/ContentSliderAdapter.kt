package com.dvach_2ch.a2ch.ui.pictures

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.util.gone
import com.dvach_2ch.a2ch.util.myOptions
import com.dvach_2ch.a2ch.util.visible
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory


class ContentSliderAdapter(
    private val ctx: Context,
    private val urls: ArrayList<String>
) : PagerAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private val players = HashMap<Int, SimpleExoPlayer>()
    private val playerViews = HashMap<Int, PlayerView>()
    init {
        layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater!!.inflate(R.layout.content_row, container, false)

        val video = view.findViewById(R.id.video) as PlayerView
        val image = view.findViewById(R.id.photo) as PhotoView
        val progressBar = view.findViewById(R.id.progress) as ProgressBar


        val url = urls[position]

        if (url.endsWith("mp4") || url.endsWith("webm")) {
            image.gone()
            video.visible()
            setupVideo(video, url, progressBar, position)
        } else {
            image.visible()
            video.visible()
            setupImage(image, url, progressBar)
        }
        container.addView(view)
        return view
    }


    private fun setupImage(image: ImageView, url: String, progressBar: ProgressBar) {
        Glide.with(ctx)
            .load(url)
            .apply(myOptions.fitCenter())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.gone()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.gone()
                    return false
                }
            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(image)


    }

    private fun setupVideo(
        video: PlayerView,
        url: String,
        progressBar: ProgressBar,
        position: Int
    ) {
        val player = ExoPlayerFactory.newSimpleInstance(video.context)
        players[position] = player
        playerViews[position] = video
        video.player = player

        progressBar.visible()
        player.apply {
            prepare(createMediaSource(url))
            addListener(object : Player.DefaultEventListener() {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                    if (playbackState == ExoPlayer.STATE_BUFFERING) progressBar.visible() else progressBar.gone()
                    if (playbackState == ExoPlayer.STATE_IDLE) player.prepare(createMediaSource(url))
                }
            })
        }
    }



    fun pausePlayers() {
        try {
            players.forEach {
                it.value.playWhenReady = false
            }
        } catch (ex: Exception){}

    }

    fun releasePlayers() {
        players.forEach {
            it.value.apply {
                release()
                playWhenReady = false
                stop(true);
            }
        }

    }

    private fun createMediaSource(url: String): ExtractorMediaSource {
        return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory("2ch"))
            .createMediaSource(Uri.parse(url))
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        pausePlayers()
        container.removeView(`object` as RelativeLayout)
    }

    override fun getCount(): Int = urls.size

}