package com.dvach_2ch.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.databinding.PostRowBinding
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.ui.posts.PostsViewModel
import com.dvach_2ch.a2ch.util.gone
import com.dvach_2ch.a2ch.util.setTextViewHTML

import kotlinx.android.synthetic.main.post_row.view.*


@Suppress("DEPRECATION")
class PostListAdapter(private val viewModel: PostsViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val items = ArrayList<ThreadPost>()

    fun updateList(newList: List<ThreadPost>) {
        if(newList.size != items.size){
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
        when (holder) {
            is PostViewHolder -> holder.bind(items[position])
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: ThreadPost) {
            DataBindingUtil.bind<PostRowBinding>(itemView)?.apply {
                this.post = post
                this.viewmodel = viewModel

                itemView.setOnLongClickListener {
                    viewModel.openPostActionDialog(this.relative, post.num)
                    return@setOnLongClickListener true
                }

                setTextViewHTML(itemView.comment, post.comment, viewModel)
            }



        }
    }


}

