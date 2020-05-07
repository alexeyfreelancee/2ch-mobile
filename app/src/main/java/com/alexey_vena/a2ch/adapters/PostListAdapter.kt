package com.alexey_vena.a2ch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.alexey_vena.a2ch.databinding.PostRowBinding
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.ui.posts.PostsViewModel
import com.alexey_vena.a2ch.util.setTextViewHTML
import com.alexey_vena.a2ch.util.toast
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
            val binding = DataBindingUtil.bind<PostRowBinding>(itemView).apply {
                this?.post = post
                this?.viewmodel = viewModel
            }
            binding?.let {
                itemView.setOnLongClickListener {
                    viewModel.openPostActionDialog(binding.relative, post.num)
                    return@setOnLongClickListener true
                }

                itemView.comment.setOnLongClickListener {
                   viewModel.openPostActionDialog(binding.relative, post.num)
                    return@setOnLongClickListener true
                }

                setTextViewHTML(itemView.comment, post.comment, viewModel)


            }


        }
    }




}

