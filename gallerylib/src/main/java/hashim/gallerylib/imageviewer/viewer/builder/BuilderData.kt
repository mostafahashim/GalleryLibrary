package hashim.gallerylib.imageviewer.viewer.builder

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import hashim.gallerylib.imageviewer.listeners.OnDismissListener
import hashim.gallerylib.imageviewer.listeners.OnImageChangeListener
import hashim.gallerylib.imageviewer.loader.ImageLoader
import hashim.gallerylib.imageviewer.viewer.viewholder.DefaultViewHolderLoader
import hashim.gallerylib.imageviewer.viewer.viewholder.ViewHolderLoader

class BuilderData<T>(
    val images: List<T>,
    val imageLoader: ImageLoader<T>
) {
    var backgroundColor = Color.BLACK
    var startPosition: Int = 0
    var imageChangeListener: OnImageChangeListener? = null
    var onDismissListener: OnDismissListener? = null
    var overlayView: View? = null
    var imageMarginPixels: Int = 0
    var containerPaddingPixels = IntArray(4)
    var shouldStatusBarHide = true
    var isZoomingAllowed = true
    var isSwipeToDismissAllowed = true
    var transitionView: ImageView? = null
    var viewHolderLoader: ViewHolderLoader<T>? = DefaultViewHolderLoader()

    constructor(images: List<T>, imageLoader: ImageLoader<T>, viewHolderLoader: ViewHolderLoader<T>)
            : this(images, imageLoader) {
        this.viewHolderLoader = viewHolderLoader
    }
}