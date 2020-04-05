package com.example.a2ch.ui.make_post

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.captcha.CaptchaData
import com.example.a2ch.models.util.Error
import com.example.a2ch.models.util.WARNING
import com.example.a2ch.util.Event
import com.example.a2ch.util.provideCaptchaUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendPostViewModel(private val repository: Repository) : ViewModel() {
    var username = ""
    var comment = ""
    var captchaAnswer = ""
    var board = ""
    var thread = ""


    private val _captchaData = MutableLiveData<CaptchaData>()
    val captchaData: LiveData<CaptchaData> = _captchaData

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> = _error

    private val _success = MutableLiveData<Any>()
    val success: LiveData<Any> = _success

    init {
        loadCaptchaInfo(null)
    }

    fun makePost(view: View?) {
        if (checkFields()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val success = repository.makePostWithCaptcha(
                        username, board,
                        thread, comment,
                        captchaAnswer, _captchaData.value!!.id
                    )
                    computeResult(success)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    _error.postValue(
                        Event(
                            Error(WARNING, "Ошибка")
                        )
                    )
                }
            }
        }
    }

    private fun computeResult(success: Boolean){
        if(!success){
            _error.postValue(
                Event(
                    Error(WARNING, "Абу пидор, капча не валидна")
                )
            )
        } else{
            _success.postValue(Any())
        }
    }

    fun loadCaptchaInfo(view: View?) {
        CoroutineScope(Dispatchers.IO).launch {
            val captchaData = repository.getCaptchaData(board, thread)
            captchaData.url = provideCaptchaUrl(captchaData.id)
            _captchaData.postValue(captchaData)
        }

    }

    private fun checkFields(): Boolean {
        if (comment.length > 14999 || comment.trim().isEmpty()) {
            _error.postValue(
                Event(
                    Error(WARNING, "Введите текст")
                )
            )
            return false
        } else if (captchaAnswer.isEmpty()) {
            _error.postValue(
                Event(
                    Error(WARNING, "Введите капчу")
                )
            )
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