package com.example.a2ch.ui.threads

import android.util.Log
import androidx.lifecycle.*
import com.example.a2ch.data.Repository
import com.example.a2ch.models.category.CategoryBase
import com.example.a2ch.models.category.Thread
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CategoryViewModel(private val repository: Repository) : ViewModel() {
    private val _category = MutableLiveData<CategoryBase>()
    val category: LiveData<CategoryBase> get() = _category

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    val threads: LiveData<List<Thread>> = Transformations.map(_category){
        it.threads
    }

    var categoryName = ""

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _startActivity = MutableLiveData<Event<String>>()
    val startActivity: LiveData<Event<String>> get() = _startActivity

    fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            _dataLoading.postValue(true)
            Log.d("TAGG", categoryName)
            try {
                val result = repository.loadThreads(categoryName)
                _category.postValue(result)
            } catch (ex: Exception){
                _error.postValue("Доски не существует")
            }

            _dataLoading.postValue(false)
        }

    }

    fun startPostsActivity(threadNum: String){
        _startActivity.postValue(Event(threadNum))
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