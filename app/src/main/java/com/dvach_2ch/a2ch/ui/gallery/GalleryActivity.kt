package com.dvach_2ch.a2ch.ui.gallery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.adapters.GalleryListAdapter
import com.dvach_2ch.a2ch.databinding.ActivityGalleryBinding
import com.dvach_2ch.a2ch.ui.media_slider.MediaSliderActivity
import com.dvach_2ch.a2ch.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class GalleryActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: ActivityGalleryBinding
    private lateinit var viewModel: GalleryViewModel
    private val factory by instance<GalleryViewModelFactory>()
    private lateinit var adapter: GalleryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(GalleryViewModel::class.java)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)
        binding.apply {
            viewmodel = viewModel
            lifecycleOwner = this@GalleryActivity
        }
        viewModel.setupViewModel(
            intent.getStringExtra(THREAD_NUM),
            intent.getStringExtra(BOARD_NAME)
        )

        viewModel.openPhoto.observe(this, Observer {
            if (!it.hasBeenHandled) {
                startActivity(Intent(this, MediaSliderActivity::class.java).apply {
                    putExtra(POSITION, it.peekContent().getInt(POSITION))
                    putExtra(URLS, it.peekContent().getString(URLS))
                })
            }
        })
        setupPhotosList()
    }

    override fun onStart() {
        super.onStart()
        supportActionBar?.title = "Галерея"
    }

    override fun onStop() {
        super.onStop()
        supportActionBar?.title = "Двачан"
    }
    private fun setupPhotosList() {
        adapter = GalleryListAdapter(viewModel)
        binding.photosList.apply {
            layoutManager = GridLayoutManager(this@GalleryActivity, 4)
            adapter = this@GalleryActivity.adapter
        }
        viewModel.thumbs.observe(this, Observer {
            adapter.fetchList(it)
        })
    }
}
