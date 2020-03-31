package com.example.a2ch.ui.send_post

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.post.MakePostError
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendPostViewModel(private val repository: Repository) : ViewModel() {
    var username = ""
    var comment = ""
    var captchaAnswer = ""
    var board = ""
    var thread = ""
    var passcode = ""


    private val _captchaResult = MutableLiveData<String>()
    val captchaResult: LiveData<String> = _captchaResult

    private val _postResult = MutableLiveData<Event<String>>()
    val postResult: LiveData<Event<String>> = _postResult

    init {
        loadCaptchaInfo(null)
    }

    fun makePost(view: View?) {
        if (checkFields()) {
            CoroutineScope(Dispatchers.IO).launch {
                var result: MakePostError? = null

                if (_captchaResult.value != "Ошибка") {
                    result = repository.makePostWithCaptcha(
                        username, board,
                        thread, comment,
                        captchaAnswer, _captchaResult.value!!
                    )
                }

                if (result?.error != null) {
                    _postResult.postValue(Event(result.reason!!))
                } else {
                    _postResult.postValue(Event("success"))
                }

            }
        }
    }

    fun loadCaptchaInfo(view: View?) {
        CoroutineScope(Dispatchers.IO).launch {
            val info = repository.getCaptchaInfo(board, thread)

            when (info.result) {
                0 -> _captchaResult.postValue("Ошибка")
                1 -> {
                    _captchaResult.postValue(info.id)
                }
            }
        }

    }

    private fun checkFields(): Boolean {
        if (captchaAnswer.isEmpty() || comment.length > 14999) {
            return false
        }
        return true
    }
}

@Suppress("UNCHECKED_CAST")
class SendPostViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SendPostViewModel(
            repository
        ) as T
    }

}