package hashim.gallerylib.imageviewer.viewer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import hashim.gallerylib.imageviewer.common.extensions.resetScale
import hashim.gallerylib.imageviewer.common.pager.RecyclingPagerAdapter
import hashim.gallerylib.imageviewer.loader.ImageLoader
import hashim.gallerylib.imageviewer.viewer.viewholder.DefaultViewHolder
import hashim.gallerylib.imageviewer.viewer.viewholder.ViewHolderLoader

internal class ImagesPagerAdapter<T>(
    private val context: Context,
    _images: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val isZoomingAllowed: Boolean,
    private val viewHolderLoader: ViewHolderLoader<T>
) : RecyclingPagerAdapter<DefaultViewHolder<T>>() {

    private var images = _images
    private val holders = mutableListOf<DefaultViewHolder<T>>()
    private var primaryPos = -1

    companion object {
        val photoViewId = View.generateViewId()
    }

    fun isScaled(position: Int): Boolean =
        holders.firstOrNull { it.position == position }?.isScaled() ?: false

    override fun destroyItem(parent: ViewGroup, position: Int, item: Any) {
        holders.firstOrNull { it.position == position }?.destroyItem(parent, position, images[position])
            ?: false
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder<T> {
        val photoView = PhotoView(context).apply {
            id = photoViewId
            isEnabled = isZoomingAllowed
            setOnViewDragListener { _, _ -> setAllowParentInterceptOnEdge(scale == 1.0f) }
        }

        return viewHolderLoader.loadViewHolder(photoView).also {
            it.imageLoader = imageLoader
            holders.add(it)
        }
    }

    override fun onBindViewHolder(holder: DefaultViewHolder<T>, position: Int) =
        holder.bind(position, images[position])

    override fun getItemCount() = images.size

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    internal fun resetScale(position: Int) =
        holders.firstOrNull { it.position == position }?.resetScale()

    fun onDialogClosed() = holders.forEach {
        it.onDialogClosed(
            it.position,
            images[it.position]
        )
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)

        // Only fire when the primary item has actually changed
        if (position != primaryPos) {
            primaryPos = position
            holders.forEach {
                it.setIsVisible(
                    it.position == primaryPos,
                    it.position,
                    images[it.position]
                )
            }
        }
    }

}