package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.models.category.Thread
import com.example.a2ch.ui.category.CategoryViewModel
import kotlinx.android.synthetic.main.thread_row.view.*

class ThreadListAdapter(private val viewModel: CategoryViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<Thread>()

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
        if(items.size != newList.size){
            items.clear()
            items.addAll(newList)
            notifyDataSetChanged()
        }

    }

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Thread) {
            DataBindingUtil.bind<ThreadRowBinding>(itemView)?.apply {
                thread = item
                viewmodel = viewModel
            }

            Glide.with(itemView.context)
                .load("https://2ch.hk${item.files[0].path}")
                .into(itemView.thread_photo)
        }
    }
}