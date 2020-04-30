package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.FavouriteRowBinding
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.ui.favourite.FavouritesViewModel

class FavouritesAdapter(private val viewModel: FavouritesViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<ThreadPost>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ThreadViewHolder(FavouriteRowBinding.inflate(inflater, parent, false).root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder -> holder.bind(items[position])
        }
    }


    fun updateList(newList: List<ThreadPost>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): ThreadPost {
        return items[position]
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