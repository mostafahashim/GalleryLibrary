package hashim.gallerylib.imageviewer.viewer.viewholder

import com.github.chrisbanes.photoview.PhotoView

interface ViewHolderLoader<T> {

    /**
     * Fires every time a new ViewHolder should be created
     *
     * @param photoView a {@link PhotoView} object, configured to behave well
     */
    fun loadViewHolder(photoView: PhotoView): DefaultViewHolder<T>
}