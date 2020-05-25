package com.dvach_2ch.a2ch.ui.posts.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.adapters.PostListAdapter
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.ui.posts.PostsViewModel

class AnswersDialog(
   private val answers: List<ThreadPost>,
   viewModel: PostsViewModel,
    context: Context
) : Dialog(context) {
    private lateinit var postsList: RecyclerView
    private  val adapter = PostListAdapter(viewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent)))
        setContentView(R.layout.dialog_answers)
        postsList = findViewById(R.id.post_list)
        postsList.adapter = adapter
        adapter.updateList(answers)
    }


}