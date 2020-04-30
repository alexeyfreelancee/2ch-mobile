package com.example.a2ch.ui.threads


import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.a2ch.data.Repository
import com.example.a2ch.data.ThreadDataSourceFactory
import com.example.a2ch.models.threads.ThreadBase
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.CRITICAL
import com.example.a2ch.models.util.Error
import com.example.a2ch.models.util.WARNING
import com.example.a2ch.util.Event
import com.example.a2ch.util.NO_INTERNET
import com.example.a2ch.util.isNetworkAvailable
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
        loadCategoryInfo()
        setupThreads()
    }

    private fun loadCategoryInfo() {
        viewModelScope.launch {
            _dataLoading.value = true
            try {
                val threadBase = repository.loadBoardInfo(boardName)
                _category.value = threadBase
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