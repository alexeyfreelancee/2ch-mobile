package com.alexey_vena.a2ch.ui.threads


import android.os.Handler
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.alexey_vena.a2ch.data.Repository
import com.alexey_vena.a2ch.models.threads.ThreadBase
import com.alexey_vena.a2ch.models.threads.ThreadPost
import com.alexey_vena.a2ch.models.util.CRITICAL
import com.alexey_vena.a2ch.models.util.Error
import com.alexey_vena.a2ch.models.util.WARNING
import com.alexey_vena.a2ch.util.Event
import com.alexey_vena.a2ch.util.NO_INTERNET
import com.alexey_vena.a2ch.util.isNetworkAvailable
import com.alexey_vena.a2ch.util.log
import kotlinx.coroutines.launch


class ThreadsViewModel(private val repository: Repository) : ViewModel() {
    private val _category = MutableLiveData<ThreadBase>()
    val category: LiveData<ThreadBase> get() = _category

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> get() = _error

    var threads: LiveData<PagedList<ThreadPost>> = MutableLiveData()

    private var boardName = ""

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _startPostsActivity = MutableLiveData<Event<String>>()
    val startPostsActivity: LiveData<Event<String>> get() = _startPostsActivity


    fun setBoardName(board: String) {
        boardName = board
        loadData()
    }

    fun loadData() {
        _dataLoading.value = true
        checkErrors()
        setupThreads()
        Handler().postDelayed({
            _dataLoading.value = false
        }, 500)
    }

    private fun checkErrors() = viewModelScope.launch {
        val threadBase = repository.loadBoardInfo(boardName)
        if (threadBase == null) {
            error()
        } else {
            if (threadBase.threadItems.isEmpty()) error()
            _category.value = threadBase
        }
    }

    fun error() {
        _error.value = (Event(Error(CRITICAL, "Доски не существует")))
    }

    private fun setupThreads() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .build()


        threads = LivePagedListBuilder(
            ThreadDataSourceFactory(
                repository,
                boardName
            ), config
        ).build()
    }

    fun startPostsActivity(threadNum: String) {
        viewModelScope.launch {
            if (isNetworkAvailable() || repository.getThreadFromDb(boardName, threadNum) != null) {
                _startPostsActivity.postValue(Event(threadNum))
            } else {
                _error.value = Event(Error(WARNING, NO_INTERNET))
            }
        }
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