package com.example.a2ch.ui.thread_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.data.Repository

class ThreadsViewModel(private val repository: Repository) : ViewModel() {
    // TODO: Implement the ViewModel
}


@Suppress("UNCHECKED_CAST")
class ThreadsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ThreadsViewModel(
            repository
        ) as T
    }

}