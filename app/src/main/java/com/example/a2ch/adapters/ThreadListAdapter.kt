package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.models.category.Thread
import com.example.a2ch.ui.threads.CategoryViewModel
import kotlinx.android.synthetic.main.thread_row.view.*
import java.util.*
import kotlin.collections.ArrayList

class ThreadListAdapter(private val viewModel: CategoryViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val pattern = constraint.toString().toLowerCase(Locale.ROOT).trim()
                val filteredList = ArrayList<Thread>()

                if (pattern.isEmpty()) {
                    items.addAll(itemsFull)
                } else {
                    itemsFull.forEach {
                        if (
                            it.subject.contains(pattern) ||
                            it.comment.contains(pattern)
                        ) {
                            filteredList.add(it)
                        }
                    }
                }

                val result = FilterResults()
                result.values = filteredList
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val resultList = results?.values as List<Thread>
                items.clear()
                items.addAll(resultList)
                notifyDataSetChanged()
            }
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