package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.BoardRowBinding
import com.example.a2ch.models.boards.Board
import com.example.a2ch.ui.boards.BoardsViewModel
import java.util.*
import kotlin.collections.ArrayList


class BoardListAdapter(private val viewModel: BoardsViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val items = ArrayList<Board>()
    private var itemsFull = ArrayList<Board>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BoardRowBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> holder.bind(items[position])
        }
    }

    fun updateList(newList: List<Board>) {
        if(items.size != newList.size){
            items.clear()
            items.addAll(newList)
            itemsFull = ArrayList(newList)
            notifyDataSetChanged()
        }

    }

    //Не трогай эту ебанину, сам не понимаю как работает
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<Board>()
                val pattern = constraint.toString().toLowerCase(Locale.ROOT).trim()

                if (pattern.isEmpty()) {
                    filteredList.addAll(itemsFull)
                } else {
                    itemsFull.forEach {
                        if (it.name.toLowerCase(Locale.ROOT).contains(pattern) || it.id.toLowerCase(Locale.ROOT).contains(pattern)) {
                            if(it.isHeader){
                                //Поиск по категории
                                filteredList.add(it)
                                val minIndex = itemsFull.indexOf(it)
                                for ((index, param) in itemsFull.withIndex()){
                                    if(index > minIndex){
                                        if(param.isHeader) return@forEach
                                        filteredList.add(param)
                                    }
                                }
                            }else {
                                //Обычный поиск
                                filteredList.add(it)
                            }
                        }
                    }
                }

                val result = FilterResults()
                result.values = filteredList
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val resultList = results?.values as List<Board>
                items.clear()
                items.addAll(resultList)
                notifyDataSetChanged()
            }
        }
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Board) {
            DataBindingUtil.bind<BoardRowBinding>(itemView).apply {
                this?.category = item
                this?.viewmodel = viewModel
            }
        }
    }
}