package com.example.a2ch.ui.posts

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import com.example.a2ch.data.Repository
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.ContentDialogData
import com.example.a2ch.models.util.Error
import com.example.a2ch.models.util.WARNING
import com.example.a2ch.util.Event
import com.example.a2ch.util.isWebLink
import com.example.a2ch.util.log
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PostsViewModel(private val repository: Repository) : ViewModel() {
    private val _posts = MutableLiveData<List<ThreadPost>>()
    val posts: LiveData<List<ThreadPost>> = _posts

    private val _scrollToBottom = MutableLiveData<Event<Any>>()
    val scrollToBottom: LiveData<Event<Any>> = _scrollToBottom

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

    private val _unreadPosts = MutableLiveData<Int>()
    val unreadPosts: LiveData<Int> = _unreadPosts

    var threadNum = ""
    var board = ""

    fun loadPosts(direction: SwipyRefreshLayoutDirection) {
        CoroutineScope(Dispatchers.IO).launch {

            _dataLoading.postValue(true)
            try {
                val postList = repository.loadPosts(threadNum, board)
                _posts.postValue(postList)

                if (direction == SwipyRefreshLayoutDirection.BOTTOM)
                    _scrollToBottom.postValue(Event(Any()))
            } catch (ex: Exception) {
                ex.printStackTrace()
                _error.postValue (
                    Event(
                        Error(CRITICAL, "Тред умер")
                    )
                )
            }
            _dataLoading.postValue(false)

        }
    }

    fun getUnreadPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            val newPosts = repository.computeUnreadPosts(threadNum, board)
            newPosts?.let {
                _unreadPosts.postValue(it)
            }
        }
    }

    fun readPost(position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.readPost(board, threadNum, position)
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
                Event(
                    Error(WARNING, "Нет подключения к интернету")
                )
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

    fun openContentDialog(post: ThreadPost, position: Int) {
        val urls = arrayListOf<String>()
        post.files.forEach {
            urls.add("https://2ch.hk${it.path}")
        }
        val contentDialogData = ContentDialogData(urls, position)
        _openContentDialog.postValue(Event(contentDialogData))
    }

    fun makeViewScreenshot(view: View) {
        val context = view.context
        repository.makeThreadScreenshot(view)
        vibrate(context)
    }

    private fun vibrate(context: Context) {
        val vibrator = getSystemService<Vibrator>(context, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    500, VibrationEffect.EFFECT_CLICK
                )
            )
        } else {
            vibrator?.vibrate(500)
        }
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