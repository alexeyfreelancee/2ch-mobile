package com.example.a2ch.ui.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.post.Post

import com.example.a2ch.util.Event
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: Repository) : ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts : LiveData<List<Post>> = _posts

    private val _scrollToBottom = MutableLiveData<Event<Any>>()
    val scrollToBottom : LiveData<Event<Any>> = _scrollToBottom

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading : LiveData<Boolean> = _dataLoading

    private val _openPhotoDialog = MutableLiveData<Event<String>>()
    val openPhotoDialog : LiveData<Event<String>> = _openPhotoDialog

    private val _openPostDialog = MutableLiveData<Event<Post>>()
    val openPostDialog : LiveData<Event<Post>> = _openPostDialog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    var thread = ""
    var board = ""

    fun loadPosts(direction: SwipyRefreshLayoutDirection){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                _dataLoading.postValue(true)

                val postList = repository.loadPosts(thread, board)
                _posts.postValue(postList)

                if(direction == SwipyRefreshLayoutDirection.BOTTOM)
                    _scrollToBottom.postValue(Event(Any()))

                _dataLoading.postValue(false)
            } catch (ex: Exception){
                _error.postValue("Треда не существует")
            }

        }

    }

    fun openPostDialog(parent: String){
        CoroutineScope(Dispatchers.IO).launch {
            val post = repository.getPost(board, parent)
            _openPostDialog.postValue(Event(post))
        }

    }

    fun openPhotoDialog(photoUrl: String){
        _openPhotoDialog.postValue(Event(photoUrl))
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