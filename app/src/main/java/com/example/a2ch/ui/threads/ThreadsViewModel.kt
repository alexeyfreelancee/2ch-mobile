package com.example.a2ch.ui.threads

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.Error
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CategoryViewModel(private val repository: Repository) : ViewModel() {
    private val _category = MutableLiveData<ThreadBase>()
    val category: LiveData<ThreadBase> get() = _category

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> get() = _error

    private val _threads = MutableLiveData<List<ThreadPost>>()
    val threads: LiveData<List<ThreadPost>> = _threads

    var boardName = ""

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _startPostsActivity = MutableLiveData<Event<String>>()
    val startPostsActivity: LiveData<Event<String>> get() = _startPostsActivity

    fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            _dataLoading.postValue(true)
            Log.d("TAGG", boardName)
            try {
                val result = repository.loadBoardInfo(boardName)
                _category.postValue(result)
                parseThreads(result)
            } catch (ex: Exception) {
                ex.printStackTrace()
                _error.postValue(
                    Event(
                        Error(CRITICAL, "Доски не существует")
                    )
                )
            }

            _dataLoading.postValue(false)
        }

    }

    private fun parseThreads(result: ThreadBase) {
        val threadList = ArrayList<ThreadPost>()
        result.threadItems.forEach {
            threadList.add(it.posts[0])
        }
        _threads.postValue(threadList)
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
        return CategoryViewModel(
            repository
        ) as T
    }

}