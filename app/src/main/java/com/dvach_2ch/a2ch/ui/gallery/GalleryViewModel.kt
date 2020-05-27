package com.dvach_2ch.a2ch.ui.gallery

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.dvach_2ch.a2ch.data.Repository
import com.dvach_2ch.a2ch.models.Thumbnail
import com.dvach_2ch.a2ch.util.Event
import com.dvach_2ch.a2ch.util.POSITION
import com.dvach_2ch.a2ch.util.URLS
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: Repository) : ViewModel() {
    private val _thumbs = MutableLiveData<List<Thumbnail>>()
    val thumbs: LiveData<List<Thumbnail>> = _thumbs

    val openPhoto = MutableLiveData<Event<Bundle>>()

    val dataLoading = MutableLiveData<Boolean>()
    var threadNum = ""
    var board = ""

    fun setupViewModel(threadNum: String?, board: String?) {
        this.threadNum = threadNum ?: ""
        this.board = board ?: ""
        loadPhotos()
    }

    private fun loadPhotos() = viewModelScope.launch {
        dataLoading.value = true
        _thumbs.value = repository.loadAllThumbs(threadNum, board)

        dataLoading.value = false
    }

    fun openPhoto(position: Int) = viewModelScope.launch{
        val urls = StringBuilder()
        val photos = repository.loadAllPhotos(threadNum, board)
        photos.forEach {
            urls.append("$it,")
        }
        val bundle = bundleOf(
            POSITION to position,
            URLS to urls.toString()
        )
        openPhoto.value = Event(bundle)
    }
}

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GalleryViewModel(
            repository
        ) as T
    }

}