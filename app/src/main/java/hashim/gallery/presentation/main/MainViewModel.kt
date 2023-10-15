package hashim.gallery.presentation.main

import androidx.lifecycle.ViewModel
import hashim.gallery.adapter.RecyclerGalleryAdapter
import hashim.gallerylib.model.GalleryModel

class MainViewModel : ViewModel() {
    lateinit var observer: Observer
    var galleryModels: ArrayList<GalleryModel> = ArrayList()

    var recyclerGalleryAdapter: RecyclerGalleryAdapter

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