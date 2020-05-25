package com.dvach_2ch.a2ch.ui.history

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dvach_2ch.a2ch.data.Repository
import com.dvach_2ch.a2ch.models.threads.ThreadPost
import com.dvach_2ch.a2ch.models.util.StartPostsData
import com.dvach_2ch.a2ch.util.Event
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: Repository) : ViewModel() {

    var threads: LiveData<PagedList<ThreadPost>> = MutableLiveData()

    private val _startPostsActivity = MutableLiveData<Event<StartPostsData>>()
    val startPostsActivity: LiveData<Event<StartPostsData>>
        get() = _startPostsActivity

    val empty = MutableLiveData(false)

     fun loadHistory() {
      viewModelScope.launch {
          val config = PagedList.Config.Builder()
              .setEnablePlaceholders(false)
              .setPageSize(10)
              .build()

          threads = LivePagedListBuilder(
              HistoryDataSourceFactory(
                  repository,empty
              ), config
          ).build()
        }
    }

    fun startPostsActivity(thread: ThreadPost) {
        _startPostsActivity.value = Event(
            StartPostsData(thread.board, thread.num)
        )
    }
}

@Suppress("UNCHECKED_CAST")
class HistoryViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HistoryViewModel(
            repository
        ) as T
    }

}