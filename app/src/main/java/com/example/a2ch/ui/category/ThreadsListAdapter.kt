package com.example.a2ch.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.ThreadRowBinding
import com.example.a2ch.models.category.Thread

class ThreadsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val items = ArrayList<Thread>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       val inflater = LayoutInflater.from(parent.context)
        val binding = ThreadRowBinding.inflate(inflater, parent, false)
        return ThreadViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is ThreadViewHolder -> holder.bind(items[position])
        }
    }

    fun updateList(newList: List<Thread>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(item: Thread){
            DataBindingUtil.bind<ThreadRowBinding>(itemView)?.apply {
                 thread = item
            }
        }
    }
}