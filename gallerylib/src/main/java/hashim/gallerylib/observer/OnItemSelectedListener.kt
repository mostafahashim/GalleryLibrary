package hashim.gallerylib.observer

import android.widget.ImageView
import hashim.gallerylib.model.GalleryModel

interface OnItemSelectedListener {
    fun onItemSelectedListener(position: Int)
    fun onImageView(position: Int,imageView: ImageView, galleryModels: ArrayList<GalleryModel>)
}