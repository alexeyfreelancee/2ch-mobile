package com.dvach_2ch.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.databinding.PhotoRowBinding
import com.dvach_2ch.a2ch.models.Thumbnail
import com.dvach_2ch.a2ch.ui.gallery.GalleryViewModel
import com.dvach_2ch.a2ch.util.log

class GalleryListAdapter(private val viewModel: GalleryViewModel) :
    RecyclerView.Adapter<GalleryListAdapter.PhotoViewHolder>() {
    private val photos = ArrayList<Thumbnail>()

    fun fetchList(newList: List<Thumbnail>) {
        photos.clear()
        photos.addAll(newList)
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(thumbnail: Thumbnail) {
            DataBindingUtil.bind<PhotoRowBinding>(itemView)?.apply {
                this.viewmodel = viewModel
                this.thumb = thumbnail
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhotoRowBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding.root)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            viewModel.openPhoto(position)
        }
        holder.bind(photos[position])
    }
}