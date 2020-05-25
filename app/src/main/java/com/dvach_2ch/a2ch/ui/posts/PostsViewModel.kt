package com.dvach_2ch.a2ch.ui.posts

import android.content.Context
import android.view.View
import androidx.lifecycle.*
import com.dvach_2ch.a2ch.data.Repository
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.models.util.CRITICAL
import com.dvach_2ch.a2ch.models.util.ContentDialogData
import com.dvach_2ch.a2ch.models.util.Error
import com.dvach_2ch.a2ch.models.util.WARNING
import com.dvach_2ch.a2ch.util.Event
import com.dvach_2ch.a2ch.util.isWebLink
import com.dvach_2ch.a2ch.util.toast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PostsViewModel(private val repository: Repository) : ViewModel() {
    private val _posts = MutableLiveData<List<ThreadPost>>()
    val posts: LiveData<List<ThreadPost>> = _posts

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _openContentDialog = MutableLiveData<Event<ContentDialogData>>()
    val contentDialogData: LiveData<Event<ContentDialogData>> = _openContentDialog

    private val _openPostDialog = MutableLiveData<Event<ThreadPost>>()
    val openPostDialog: LiveData<Event<ThreadPost>> = _openPostDialog

    private val _openWebLink = MutableLiveData<Event<String>>()
    val openWebLink: LiveData<Event<String>> = _openWebLink

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> get() = _error

    private val _isFavourite = MutableLiveData<Boolean>()
    val isFavourite: LiveData<Boolean> get() = _isFavourite

    private val _addToFavourites = MutableLiveData<Event<Boolean>>()
    val addToFavourites: LiveData<Event<Boolean>> get() = _addToFavourites

    private val _removeFromFavourites = MutableLiveData<Event<Boolean>>()
    val removeFromFavourites: LiveData<Event<Boolean>> get() = _removeFromFavourites

    var needToShowProgress = true

    val openPostActionDialog = MutableLiveData<Event<ArrayList<Any>>>()
    val answerPost = MutableLiveData<Event<String>>()
    val showAnswers = MutableLiveData<Event<List<ThreadPost>>>()

    var threadNum = ""
    var board = ""


    fun showAnswers(postNum:String) = viewModelScope.launch{
        val answers = repository.loadAnswers(threadNum, board,postNum)
        showAnswers.value = Event(answers)
    }

    fun loadPosts() {
        viewModelScope.launch {
            if (needToShowProgress) _dataLoading.postValue(true)
            try {
                val postList = repository.loadPosts(threadNum, board)
                _posts.postValue(postList)
            } catch (ex: Exception) {
                ex.printStackTrace()
                _error.postValue(Event(Error(CRITICAL, "Тред умер")))
            }
            delay(500)
            if (needToShowProgress) _dataLoading.postValue(false)
            needToShowProgress = false
        }
    }


    fun addToFavourites() = viewModelScope.launch {
        try {
            val thread = repository.getThread(board, threadNum)
            if (thread != null) {
                repository.addToFavourites(board, thread)
                _addToFavourites.postValue(Event(true))
            }
        } catch (ex: Exception) {
            _error.postValue(
                Event(
                    Error(WARNING, "Нет подключения к интернету")
                )
            )
        }


    }

    fun removeFromFavourites() = viewModelScope.launch {
        try {
            val thread = repository.getThread(board, threadNum)
            if (thread != null) {
                repository.removeFromFavourites(thread)
                _removeFromFavourites.postValue(Event(true))
            }
        } catch (ex: Exception) {
            _error.postValue(
                Event(Error(WARNING, "Нет подключения к интернету"))
            )
        }


    }

    fun openUrl(href: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (href.isWebLink()) {
                val link = href.replace(",", "")
                _openWebLink.postValue(Event(link))
            } else {
                openPostDialog(href)
            }

        }

    }

    fun checkFavourite() = viewModelScope.launch {
        val result = repository.isFavourite(board, threadNum)
        _isFavourite.value = result
    }

    private fun openPostDialog(href: String) = viewModelScope.launch {
        try {
            val post = repository.getPost(href, threadNum)
            _openPostDialog.postValue(Event(post))
        } catch (ex: Exception) {
            ex.printStackTrace()
            _error.postValue(
                Event(
                    Error(WARNING, "Пост не найдет")
                )
            )
        }

    }

    fun downloadAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.downloadAll(threadNum, board, context)
        }
    }

    fun openPostActionDialog(view: View, postNum: String) {
        openPostActionDialog.value = Event(arrayListOf(view, postNum))
    }

    fun answerPost(postNum: String) {
        answerPost.value = Event(postNum)
    }

    fun openContentDialog(post: ThreadPost, position: Int) {
        val urls = arrayListOf<String>()
        post.files.forEach {

            urls.add("https://2ch.hk${it.path}")
        }
        val contentDialogData = ContentDialogData(urls, position)
        _openContentDialog.postValue(Event(contentDialogData))
    }

    fun makeViewScreenshot(view: View) {
        repository.makeThreadScreenshot(view)
        view.context.toast("Скриншот сохранен в галерею")
    }


}

@Suppress("UNCHECKED_CAST")
class PostsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PostsViewModel(
            repository
        ) as T
    }

}