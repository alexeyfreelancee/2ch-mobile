package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.ui.threads.ThreadsViewModel


class ThreadListAdapter(private val viewModel: ThreadsViewModel) :
    PagedListAdapter<ThreadPost, RecyclerView.ViewHolder>(ThreadDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ThreadViewHolder(ThreadRowBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder -> {
               getItem(position)?.let {
                   holder.bind(it)
                }
            }
        }
    }

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            DataBindingUtil.bind<ThreadRowBinding>(itemView)?.apply {
                thread = item
                viewmodel = viewModel
            }
        }
    }
}

class ThreadDiffUtil(
) : DiffUtil.ItemCallback<ThreadPost>() {

    override fun areItemsTheSame(oldItem: ThreadPost, newItem: ThreadPost): Boolean {
        return oldItem.num == newItem.num
    }

    override fun areContentsTheSame(old: ThreadPost, new: ThreadPost): Boolean {
        return old.name == new.name
                && old.comment == new.comment
                && old.date == new.date
                && old.postsCount == new.postsCount
    }


}

