package com.example.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a2ch.databinding.PostRowBinding
import com.example.a2ch.models.post.Post
import com.example.a2ch.ui.posts.PostsViewModel
import kotlinx.android.synthetic.main.post_row.view.*

class PostListAdapter(private val viewModel: PostsViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<Post>()

    fun updateList(newList: List<Post>){
        if(items.size != newList.size){
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
        when(holder){
            is PostViewHolder -> holder.bind(items[position])
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(post: Post){
            DataBindingUtil.bind<PostRowBinding>(itemView).apply {
                this?.post = post
                this?.viewmodel = viewModel
            }
            try {
                Glide.with(itemView.context)
                    .load("https://2ch.hk${post.files[0].path}")
                    .into(itemView.photo1)

                Glide.with(itemView.context)
                    .load("https://2ch.hk${post.files[1].path}")
                    .into(itemView.photo1)

                Glide.with(itemView.context)
                    .load("https://2ch.hk${post.files[2].path}")
                    .into(itemView.photo1)

                Glide.with(itemView.context)
                    .load(post.files[3])  .load("https://2ch.hk${post.files[3].path}")
                    .into(itemView.photo1)

                Glide.with(itemView.context)
                    .load(post.files[4])  .load("https://2ch.hk${post.files[4].path}")
                    .into(itemView.photo1)
            } catch (ex: Exception){}

        }
    }
}