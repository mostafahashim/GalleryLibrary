package hashim.gallerylib.imageviewer.loader

import android.widget.ImageView

interface ImageLoader<T> {
    fun loadImage(imageView: ImageView?, image: T?)
}