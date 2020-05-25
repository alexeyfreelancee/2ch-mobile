package com.dvach_2ch.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.databinding.ThreadRowBinding
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.ui.threads.ThreadsViewModel
import com.dvach_2ch.a2ch.util.ThreadDiffUtil


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


