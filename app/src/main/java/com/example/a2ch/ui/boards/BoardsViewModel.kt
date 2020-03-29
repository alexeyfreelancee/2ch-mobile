package com.example.a2ch.ui.boards

import androidx.lifecycle.*
import com.example.a2ch.data.Repository
import com.example.a2ch.util.Event

class BoardsViewModel() : ViewModel() {
    private val _startEvent = MutableLiveData<Event<String>>()
    val startEvent : LiveData<Event<String>> get() = _startEvent


    fun startActivity(name: String){
        _startEvent.postValue(Event(name))
    }



}

