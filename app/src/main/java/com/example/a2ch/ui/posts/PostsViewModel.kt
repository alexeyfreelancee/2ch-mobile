package com.example.a2ch.ui.posts

import android.content.ClipData
import android.content.ClipboardManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.a2ch.data.source.Repository
import com.example.a2ch.models.post.Post
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.ContentDialogData
import com.example.a2ch.models.util.Error
import com.example.a2ch.models.util.WARNING
import com.example.a2ch.util.Event
import com.example.a2ch.util.isWebLink
import com.example.a2ch.util.toast
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: Repository) : ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _scrollToBottom = MutableLiveData<Event<Any>>()
    val scrollToBottom: LiveData<Event<Any>> = _scrollToBottom

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _openContentDialog = MutableLiveData<Event<ContentDialogData>>()
    val contentDialogData: LiveData<Event<ContentDialogData>> = _openContentDialog

    private val _openPostDialog = MutableLiveData<Event<Post>>()
    val openPostDialog: LiveData<Event<Post>> = _openPostDialog

    private val _openWebLink = MutableLiveData<Event<String>>()
    val openWebLink: LiveData<Event<String>> = _openWebLink

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> get() = _error

    private val _addToFavourites = MutableLiveData<Event<String>>()
    val addToFavourites: LiveData<Event<String>> get() = _addToFavourites

    var thread = ""
    var board = ""

    fun loadPosts(direction: SwipyRefreshLayoutDirection) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _dataLoading.postValue(true)

                val postList = repository.loadPosts(thread, board)
                _posts.postValue(postList)

                if (direction == SwipyRefreshLayoutDirection.BOTTOM)
                    _scrollToBottom.postValue(Event(Any()))

                _dataLoading.postValue(false)
            } catch (ex: Exception) {
                ex.printStackTrace()
                _error.postValue(
                    Event(
                        Error(CRITICAL, "Тред умер нахуй")
                    )
                )
            }

        }

    }

    fun addToHistory(){
        CoroutineScope(Dispatchers.IO).launch {
            repository.addToHistory(board,thread)
        }
    }

    fun addToFavourites(){
        CoroutineScope(Dispatchers.IO).launch {
            val success = repository.addToFavourites(board,thread)
            if(success){
                _addToFavourites.postValue(Event("Тред добавлен в избранное"))
            } else{
                _addToFavourites.postValue(Event("Ошибка"))
            }
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

    private fun openPostDialog(href: String) = viewModelScope.launch {
        try {
            val post = repository.getPost(href)
            _openPostDialog.postValue(Event(post))
        } catch (ex: Exception) {
            _error.postValue(
                Event(
                    Error(WARNING, "Пост не найдет")
                )
            )
        }

    }


    fun openContentDialog(post: Post, position: Int) {
        val urls = arrayListOf<String>()
        post.files.forEach {
            urls.add("https://2ch.hk${it.path}")
        }
        val contentDialogData = ContentDialogData(urls, position)
        _openContentDialog.postValue(Event(contentDialogData))
    }

    fun copyToClipboard(view: View, text: String) {
        val context = view.context

        val clipboard = ContextCompat.getSystemService<ClipboardManager>(
            context,
            ClipboardManager::class.java
        )
        val clip = ClipData.newPlainText(
            "label", text
        )
        clipboard?.setPrimaryClip(clip)

        context.toast("Скопиравно в буфер обмена")
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