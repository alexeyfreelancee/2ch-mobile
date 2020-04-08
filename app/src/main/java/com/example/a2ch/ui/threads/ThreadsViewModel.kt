package com.example.a2ch.ui.threads

import android.util.Log
import androidx.lifecycle.*
import com.example.a2ch.data.Repository
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.Error
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ThreadsViewModel(private val repository: Repository) : ViewModel() {
    private val _category = MutableLiveData<ThreadBase>()
    val category: LiveData<ThreadBase> get() = _category

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> get() = _error

    val threads: LiveData<List<ThreadPost>> = Transformations.map(category) {
        val threadList = ArrayList<ThreadPost>()
        it.threadItems.forEach {
            threadList.add(it.posts[0])
        }
        threadList
    }

    var boardName = ""

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _startPostsActivity = MutableLiveData<Event<String>>()
    val startPostsActivity: LiveData<Event<String>> get() = _startPostsActivity

    fun update() {
        viewModelScope.launch {
            _dataLoading.value = true
            try {
                val result = repository.loadBoardInfo(boardName)
                _category.postValue(result)
            } catch (ex: Exception) {
                ex.printStackTrace()
                _error.postValue(
                    Event(
                        Error(CRITICAL, "Доски не существует")
                    )
                )
            }
            _dataLoading.value = false
        }
    }


    fun startPostsActivity(threadNum: String) {
        _startPostsActivity.postValue(Event(threadNum))
    }
}


@Suppress("UNCHECKED_CAST")
class CategoryViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ThreadsViewModel(
            repository
        ) as T
    }

}