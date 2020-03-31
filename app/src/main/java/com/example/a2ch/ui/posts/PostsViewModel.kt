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

    private val _openDialog = MutableLiveData<Event<String>>()
    val openDialog : LiveData<Event<String>> = _openDialog

    var thread = ""
    var board = ""

    fun loadPosts(direction: SwipyRefreshLayoutDirection){
        CoroutineScope(Dispatchers.IO).launch {
            _dataLoading.postValue(true)

            val postList = repository.loadPosts(thread, board)
            _posts.postValue(postList)

            if(direction == SwipyRefreshLayoutDirection.BOTTOM)
                _scrollToBottom.postValue(Event(Any()))

            _dataLoading.postValue(false)
        }

    }


    fun openDialog(photoUrl: String){
        _openDialog.postValue(Event(photoUrl))
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