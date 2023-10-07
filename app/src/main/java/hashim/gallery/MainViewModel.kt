package hashim.gallery

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hashim.gallerylib.model.GalleryModel

class MainViewModel: ViewModel() {
    lateinit var observer:Observer
    lateinit var galleryModel: GalleryModel
    var avatar = MutableLiveData("")

    fun isGalleryModelInitialized(): Boolean {
        return ::galleryModel.isInitialized
    }

    fun openGallery() {

    }

    interface Observer {
        fun openGallery()
    }
}