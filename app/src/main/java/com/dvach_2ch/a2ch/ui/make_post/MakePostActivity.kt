package com.dvach_2ch.a2ch.ui.make_post

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.databinding.ActivityMakePostBinding
import com.dvach_2ch.a2ch.ui.make_post.captcha_dialog.CaptchaDialog
import com.dvach_2ch.a2ch.ui.make_post.captcha_dialog.CaptchaListener
import com.dvach_2ch.a2ch.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MakePostActivity : AppCompatActivity(), KodeinAware, CaptchaListener {
    override val kodein by kodein()
    private val factory: SendPostViewModelFactory by instance()
    private lateinit var viewModel: SendPostViewModel
    private lateinit var binding : ActivityMakePostBinding
    private var thread: String? = null
    private var board: String? = null
    private var threadAnswer:String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(checkDarkTheme()) setTheme(R.style.Dark)
        viewModel = ViewModelProvider(this, factory).get(SendPostViewModel::class.java)
        binding = DataBindingUtil.setContentView<ActivityMakePostBinding>(
            this, R.layout.activity_make_post
        ).apply {
            viewmodel = viewModel
            lifecycleOwner = this@MakePostActivity
        }
        initViewModelData()
        initObservers()
    }

    private fun initViewModelData() {
        threadAnswer = intent.getStringExtra(THREAD_ANSWER)
        thread = intent.getStringExtra(THREAD_NUM)
        board = intent.getStringExtra(BOARD_NAME)
        if(thread!=null) viewModel.thread = thread!!
        if(board!=null) viewModel.board = board!!
        if(threadAnswer!=null) {
            val text = ">>${threadAnswer}\n"
            viewModel.comment = text
            binding.editText.setText(text)
            binding.editText.setSelection(binding.editText.text.length)
        }
        viewModel.checkNamesEnabled()
    }


    private fun initObservers() {
        viewModel.error.observe(this, Observer {
          if(!it.hasBeenHandled)  initError(this, it.peekContent())
        })
        viewModel.success.observe(this, Observer {
            toast("Пост добавлен")
            finish()
        })
        viewModel.openCaptchaDialog.observe(this, Observer {
            try {
                val dialog = CaptchaDialog(this, it.peekContent(), this)
                dialog.show()
            } catch (ex:Exception){}

        })
    }

    override fun captchaPassed(response: String) {
        viewModel.captchaPassed(response)
    }


}
