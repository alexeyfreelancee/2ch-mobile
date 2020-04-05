package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.FavouriteRowBinding
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.models.threads.ThreadItem
import com.example.a2ch.ui.threads.CategoryViewModel
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.ui.favourite.FavouritesViewModel
import kotlin.collections.ArrayList


class ThreadListAdapter(private val viewModel: ViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<ThreadPost>()
    private var itemsFull = ArrayList<ThreadPost>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val root = if(viewModel is CategoryViewModel){
            ThreadRowBinding.inflate(inflater, parent, false).root
        } else {
            FavouriteRowBinding.inflate(inflater, parent, false).root
        }

        return ThreadViewHolder(root)
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
        itemsFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): ThreadPost{
        return items[position]
    }
    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
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