package com.example.a2ch.ui.category_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.models.boards.Category
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: Repository) : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _startActivity = MutableLiveData<Event<String>>()
    val startActivity: LiveData<Event<String>> = _startActivity

    var categoryName = ""

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> get() = _dataLoading


    init {
        loadCategories()
    }

    fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            _dataLoading.postValue(true)
            val boards = repository.loadBoards()
            _categories.postValue(calculateName(boards))
            _dataLoading.postValue(false)
        }
    }


    fun startThreadActivity(id: String) {
        _startActivity.postValue(Event(id))
    }

    private fun calculateName(boards: BoardsBase): List<Category> {
        return when (categoryName) {
            "Взрослым" -> boards.adult!!
            "Игры" -> boards.games!!
            "Политика" -> boards.politics!!
            "Кастом" -> boards.userBoards!!
            "Разное" -> boards.sundry!!
            "Творчество" -> boards.art!!
            "Тематика" -> boards.thematics!!
            "Техника и софт" -> boards.it!!
            "Японская культура" -> boards.japanese!!
            else -> emptyList()
        }
    }

}

@Suppress("UNCHECKED_CAST")
class CategoriesViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CategoriesViewModel(
            repository
        ) as T
    }

}