package com.example.a2ch.util

import androidx.recyclerview.widget.DiffUtil
import com.example.a2ch.models.threads.ThreadPost

class ThreadPostsCallback(
    private val oldList: List<ThreadPost>, private val newList: List<ThreadPost>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.num == new.num
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.comment == new.comment && old.name == new.name
    }

}