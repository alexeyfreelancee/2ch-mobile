package com.dvach_2ch.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.databinding.FavouriteRowBinding
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.ui.favourite.FavouritesViewModel
import com.dvach_2ch.a2ch.util.ThreadDiffUtil

class FavouritesAdapter(private val viewModel: FavouritesViewModel?) :
    PagedListAdapter<ThreadPost, RecyclerView.ViewHolder>(ThreadDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ThreadViewHolder(FavouriteRowBinding.inflate(inflater, parent, false).root)
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder ->{
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    fun loadItem(position: Int) : ThreadPost?{
        return getItem(position)
    }
    fun removeItem(position: Int){
        notifyItemRemoved(position)
    }


    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            DataBindingUtil.bind<FavouriteRowBinding>(itemView)?.apply {
                thread = item
                viewmodel = viewModel
            }
        }
    }


}

