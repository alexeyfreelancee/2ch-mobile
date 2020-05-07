package com.alexey_vena.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexey_vena.a2ch.databinding.HistoryRowBinding
import com.alexey_vena.a2ch.databinding.TimeRowBinding
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.history.HistoryViewModel
import com.alexey_vena.a2ch.util.ThreadDiffUtil
import com.alexey_vena.a2ch.util.log

class HistoryListAdapter(private val viewModel: HistoryViewModel?) :
    PagedListAdapter<ThreadPost, RecyclerView.ViewHolder>(ThreadDiffUtil()) {
    private val THREAD = 1
    private val TIME = 0



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            THREAD -> ThreadViewHolder(HistoryRowBinding.inflate(inflater, parent, false).root)
            TIME -> DateViewHolder(TimeRowBinding.inflate(inflater, parent, false).root)
            else -> throw Exception("history adapter took some heroin")
        }

    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder -> holder.bind(getItem(position)!!)
            is DateViewHolder -> holder.bind(getItem(position)!!)
        }
    }



    override fun getItemViewType(position: Int): Int {
        val thread = getItem(position)!!
        return if (thread.isDate) {
            TIME
        } else {
            THREAD
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            DataBindingUtil.bind<TimeRowBinding>(itemView)?.apply {
                thread = item
            }
        }
    }

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            DataBindingUtil.bind<HistoryRowBinding>(itemView)?.apply {
                thread = item
                viewmodel = viewModel
            }
        }
    }

}