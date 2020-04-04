package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.a2ch.databinding.FavouriteRowBinding
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.ui.threads.CategoryViewModel
import kotlinx.android.synthetic.main.thread_row.view.*
import com.example.a2ch.models.category.Thread
import com.example.a2ch.ui.favourite.FavouritesViewModel
import java.util.*
import kotlin.collections.ArrayList


class ThreadListAdapter(private val viewModel: ViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<Thread>()
    private var itemsFull = ArrayList<Thread>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ThreadRowBinding.inflate(inflater, parent, false)
        return ThreadViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder -> holder.bind(items[position])
        }
    }

    fun updateList(newList: List<Thread>) {
        items.clear()
        items.addAll(newList)
        itemsFull = ArrayList(newList)
        notifyDataSetChanged()
    }



    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Thread) {
            if(viewModel is CategoryViewModel){
                DataBindingUtil.bind<ThreadRowBinding>(itemView)?.apply {
                    thread = item
                    viewmodel = viewModel
                }
            } else if (viewModel is FavouritesViewModel){
                DataBindingUtil.bind<FavouriteRowBinding>(itemView)?.apply {
                    thread = item
                    viewmodel = viewModel
                }
            }


        }
    }
}