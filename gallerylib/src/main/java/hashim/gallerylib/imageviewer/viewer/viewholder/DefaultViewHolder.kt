package hashim.gallerylib.imageviewer.viewer.viewholder

import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import hashim.gallerylib.imageviewer.common.extensions.resetScale
import hashim.gallerylib.imageviewer.common.pager.RecyclingPagerAdapter
import hashim.gallerylib.imageviewer.loader.ImageLoader
import hashim.gallerylib.imageviewer.viewer.adapter.ImagesPagerAdapter

open class DefaultViewHolder<T>(itemView: View) : RecyclingPagerAdapter.ViewHolder(itemView) {

    internal var imageLoader: ImageLoader<T>? = null

    // If a subclass has incorporated the PhotoView into a ViewGroup, find it
    // to ensure correct behavior by default
    private var photoView: PhotoView? =
        if (itemView is PhotoView) itemView
        else itemView.findViewById(ImagesPagerAdapter.photoViewId)

    // Subclasses should return True when they wish to handle Back button presses
    // (e.g. when the image is zoomed in and should be un-zoomed when Back is pressed)
    open fun isScaled(): Boolean = (photoView?.scale ?: 1f) > 1f

    // Subclasses can respond to Back button presses here when isScaled() returns True
    open fun resetScale() = photoView?.resetScale(animate = true)

    open fun bind(position: Int, model: T) {
        this.position = position
        imageLoader?.loadImage(photoView, model)
    }

    // Subclasses may respond when the dialog window is closed (e.g. to stop video playback)
    open fun onDialogClosed(position: Int, model: T) {}

    // Subclasses may respond when this ViewHolder's View moves on or off the screen
    open fun setIsVisible(isVisible: Boolean, position: Int, model: T) {}
    open fun destroyItem(parent: ViewGroup, position: Int, model: T) {}

}