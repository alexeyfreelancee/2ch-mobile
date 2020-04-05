package com.example.a2ch.adapters

import android.graphics.Color
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ch.databinding.PostRowBinding
import com.example.a2ch.models.threads.ThreadPost

import com.example.a2ch.ui.posts.PostsViewModel
import com.example.a2ch.util.PostsAdapterListener
import com.example.a2ch.util.log
import com.example.a2ch.util.setTextViewHTML
import kotlinx.android.synthetic.main.post_row.view.*
import java.util.*
import kotlin.collections.ArrayList


class PostListAdapter(private val viewModel: PostsViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<ThreadPost>()
    var listener: PostsAdapterListener? = null

    fun updateList(newList: List<ThreadPost>) {
        if (items.size != newList.size) {
            items.clear()
            items.addAll(newList)

            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PostRowBinding.inflate(inflater, parent, false)
        return PostViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> listener?.upReached()
            items.size - 1 -> listener?.bottomReached()
        }
        when (holder) {
            is PostViewHolder -> holder.bind(items[position])
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: ThreadPost) {
            DataBindingUtil.bind<PostRowBinding>(itemView).apply {
                this?.post = post
                this?.viewmodel = viewModel
            }
            itemView.comment.setOnLongClickListener {
                viewModel.copyToClipboard(it, Html.fromHtml(post.comment).toString())
                return@setOnLongClickListener true
            }
            setTextViewHTML(itemView.comment, post.comment, viewModel)
        }
    }



}