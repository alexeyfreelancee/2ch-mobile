package com.example.a2ch.ui.boards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository
import com.example.a2ch.models.boards.Board
import com.example.a2ch.models.boards.BoardsBase
import com.example.a2ch.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BoardsViewModel(private val repository: Repository) : ViewModel() {
    private val _startCategory = MutableLiveData<Event<String>>()
    val startCategory: LiveData<Event<String>> = _startCategory

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> get() = _dataLoading

    private val _boards = MutableLiveData<List<Board>>()
    val boards: LiveData<List<Board>> = _boards

    private val _success = MutableLiveData<Boolean>(true)
    val success : LiveData<Boolean> = _success

    init {
        loadBoards()
    }

    fun loadBoards() {
        CoroutineScope(Dispatchers.IO).launch {
            _dataLoading.postValue(true)

            try {
                val boards = repository.loadBoards()
                val resultList = getResultList(boards)

                _boards.postValue(resultList)
                _success.postValue(true)
            } catch (ex: Exception) {
                _success.postValue(false)
                ex.printStackTrace()
            }

            _dataLoading.postValue(false)
        }

    }

    fun startThreadActivity(id: String) {
        _startCategory.postValue(Event(id))
    }


    private fun getResultList(boards: BoardsBase): ArrayList<Board> {
        val resultList = ArrayList<Board>()
        resultList.add(Board(name = "Разное", isHeader = true))
        resultList.addAll(boards.sundry!!)
        resultList.add(Board(name = "Тематика", isHeader = true))
        resultList.addAll(boards.thematics!!)
        resultList.add(Board(name = "Творчество", isHeader = true))
        resultList.addAll(boards.art!!)
        resultList.add(Board(name = "Политика", isHeader = true))
        resultList.addAll(boards.politics!!)
        resultList.add(Board(name = "Техника и софт", isHeader = true))
        resultList.addAll(boards.it!!)
        resultList.add(Board(name = "Игры", isHeader = true))
        resultList.addAll(boards.games!!)
        resultList.add(Board(name = "Японская культура", isHeader = true))
        resultList.addAll(boards.japanese!!)
        resultList.add(Board(name = "Взрослым", isHeader = true))
        resultList.addAll(boards.adult!!)
        resultList.add(Board(name = "Пользовательские", isHeader = true))
        resultList.addAll(boards.userBoards!!)

        return resultList
    }
}

@Suppress("UNCHECKED_CAST")
class BoardsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BoardsViewModel(
            repository
        ) as T
    }

}

