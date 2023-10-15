package hashim.gallerylib.observer

import hashim.gallerylib.model.GalleryModel

interface OnResultCallback {
    fun onResult(list: ArrayList<GalleryModel>)
}