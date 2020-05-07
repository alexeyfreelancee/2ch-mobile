package com.alexey_vena.a2ch.util

import androidx.recyclerview.widget.DiffUtil
import com.alexey_vena.a2ch.models.threads.ThreadPost

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
