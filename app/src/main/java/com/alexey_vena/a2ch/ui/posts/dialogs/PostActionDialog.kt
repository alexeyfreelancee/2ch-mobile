package com.alexey_vena.a2ch.ui.posts.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.ui.posts.PostsViewModel

class PostActionDialog(
    context: Context,
    private val viewModel: PostsViewModel,
    private val view: View,
    private val openedPostNum:String
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent)));
        setContentView(R.layout.dialog_post_action)


        findViewById<TextView>(R.id.make_screen).setOnClickListener {
            viewModel.makeViewScreenshot(view)
            dismiss()
        }

        findViewById<TextView>(R.id.answer).setOnClickListener {
            viewModel.answerPost(openedPostNum)
            dismiss()
        }
    }
}