package com.example.a2ch.ui.make_post

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.util.Event
import com.example.a2ch.util.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendPostViewModel(private val repository: Repository) : ViewModel() {
    var username = ""
    var comment = ""
    var captchaAnswer = ""
    var board = ""
    var thread = ""

    private val _captchaId = MutableLiveData<String>()
    val captchaId : LiveData<String> = _captchaId

    private val _postResult = MutableLiveData<Event<String>>()
    val postResult: LiveData<Event<String>> = _postResult

    init {
        loadCaptchaImage(null)
    }

    fun makePost(view : View?) {
        if (checkFields()) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.makePost(
                    username, board,
                    thread, comment,
                    captchaAnswer, _captchaId.value!!
                )
                log(captchaAnswer)
                log(board)
                log(thread)
                _postResult.postValue(Event("aue"))
            }
        }
    }

     fun loadCaptchaImage(view: View?){
        CoroutineScope(Dispatchers.IO).launch {
            val id = repository.getCaptchaPublicKey()
            _captchaId.postValue(id)
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