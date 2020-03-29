package com.example.a2ch.ui.category_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.CategoryRowBinding
import com.example.a2ch.models.boards.Category

class CategoriesListAdapter(private val viewModel: CategoriesViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CategoryRowBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> holder.bind(items[position])
        }
    }

    fun updateList(newList: List<Category>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Category) {
            DataBindingUtil.bind<CategoryRowBinding>(itemView).apply {
                this?.category = item
                this?.viewmodel = viewModel
            }
        }
    }
}