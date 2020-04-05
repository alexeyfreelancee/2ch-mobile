package com.example.a2ch.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.threads.ThreadPost
import com.example.a2ch.models.util.StartPostsData
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: Repository) : ViewModel() {
    private val _threads = MutableLiveData<List<ThreadPost>>()
    val threads: LiveData<List<ThreadPost>>
        get() = _threads

    private val _startPostsActivity = MutableLiveData<Event<StartPostsData>>()
    val startPostsActivity: LiveData<Event<StartPostsData>>
        get() = _startPostsActivity



     fun loadHistory(){
        CoroutineScope(Dispatchers.IO).launch {
            val threads = repository.loadHistory()
            _threads.postValue(threads)
        }
    }

    fun startPostsActivity(thread: ThreadPost) {
        _startPostsActivity.postValue(Event(
            StartPostsData(thread.board, thread.num)
        ))
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