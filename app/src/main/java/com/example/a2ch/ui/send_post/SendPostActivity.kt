package com.example.a2ch.ui.send_post

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.a2ch.R
import com.example.a2ch.databinding.ActivityMakePostBinding
import com.example.a2ch.util.*
import kotlinx.android.synthetic.main.activity_make_post.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SendPostActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: SendPostViewModelFactory by instance()
    private lateinit var viewModel: SendPostViewModel
    private lateinit var thread: String
    private lateinit var board: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(SendPostViewModel::class.java)

        DataBindingUtil.setContentView<ActivityMakePostBinding>(
            this, R.layout.activity_make_post
        ).apply {
            viewmodel = viewModel
            lifecycleOwner = this@SendPostActivity
        }

        initViewModelData()
        initObservers()
    }

    private fun initViewModelData() {
        thread = intent.getStringExtra(THREAD_NUM)
        board = intent.getStringExtra(BOARD_NAME)
        viewModel.apply {
            thread = this@SendPostActivity.thread
            board = this@SendPostActivity.board
        }

    }


    private fun initObservers() {
        viewModel.captchaResult.observe(this, Observer {
            if(it != "Ошибка"){
                captchaAnswer.setText("")
                Glide.with(applicationContext)
                    .load(provideCaptchaUrl(it))
                    .error(R.drawable.common_google_signin_btn_text_light_focused)
                    .into(captcha_img)
            } else {
                captchaAnswer.setText("")
                toast("Ошибка")
            }

        })

        viewModel.postResult.observe(this, Observer {
            val result = it.peekContent()

            if (result == "success") {
                finish()
            } else {
                toast("Сука, в рот ебал абу, капча не верна")
            }
        })
    }

}
