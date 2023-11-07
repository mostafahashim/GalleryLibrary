package hashim.gallery.presentation.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hashim.gallery.adapter.RecyclerGalleryAdapter
import hashim.gallerylib.model.GalleryModel

class MainViewModel : ViewModel() {
    lateinit var observer: Observer
    var galleryModels: ArrayList<GalleryModel> = ArrayList()

    var recyclerGalleryAdapter: RecyclerGalleryAdapter
    var isDialog = MutableLiveData(true)
    var isOpenEdit = MutableLiveData(true)
    var isRTL = MutableLiveData(true)
    var isShowImages = MutableLiveData(true)
    var isShowVideos = MutableLiveData(true)
    var count = MutableLiveData("100")
    var columns = MutableLiveData("3")

    init {
        recyclerGalleryAdapter = RecyclerGalleryAdapter(galleryModels)
    }
    fun initAdapter(models: ArrayList<GalleryModel>) {
        galleryModels = models
        recyclerGalleryAdapter = RecyclerGalleryAdapter(galleryModels)
    }

    fun openGallery() {

    }

    interface Observer {
        fun openGallery()
    }
}