package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.HistoryRowBinding
import com.example.a2ch.databinding.TimeRowBinding
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.ui.history.HistoryViewModel
import com.example.a2ch.util.log

class HistoryListAdapter(private val viewModel: HistoryViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val THREAD = 1
    private val TIME = 0

    private val items = ArrayList<ThreadPost>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            THREAD -> ThreadViewHolder(HistoryRowBinding.inflate(inflater, parent, false).root)
            TIME -> DateViewHolder(TimeRowBinding.inflate(inflater, parent, false).root)
            else -> throw Exception("history adapter took some heroin")
        }

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThreadViewHolder -> holder.bind(items[position])
            is DateViewHolder -> holder.bind(items[position])
        }
    }

    fun updateList(newList: List<ThreadPost>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val thread = items[position]
        return if (thread.isDate) {
            TIME
        } else {
            THREAD
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            log("date")
            DataBindingUtil.bind<TimeRowBinding>(itemView)?.apply {
                thread = item
            }
        }
    }

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ThreadPost) {
            log("thread")
            DataBindingUtil.bind<HistoryRowBinding>(itemView)?.apply {
                thread = item
                viewmodel = viewModel
            }
        }
    }

}