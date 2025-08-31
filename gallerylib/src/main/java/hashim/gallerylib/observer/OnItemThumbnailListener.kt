package hashim.gallerylib.observer

import android.widget.ImageView
import hashim.gallerylib.model.GalleryModel

interface OnItemThumbnailListener {
    fun onItemSelectedListener(position: Int)
    fun onItemDeleted(position: Int)
}