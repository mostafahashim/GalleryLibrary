package hashim.gallerylib.imageviewer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import hashim.gallerylib.R
import hashim.gallerylib.imageviewer.listeners.OnDismissListener
import hashim.gallerylib.imageviewer.listeners.OnImageChangeListener
import hashim.gallerylib.imageviewer.loader.ImageLoader
import hashim.gallerylib.imageviewer.viewer.builder.BuilderData
import hashim.gallerylib.imageviewer.viewer.dialog.ImageViewerDialog
import hashim.gallerylib.imageviewer.viewer.viewholder.ViewHolderLoader
import java.util.Arrays
import kotlin.math.roundToInt


class ImageViewer<T>(context: Context, builderData: BuilderData<T>) {

    private var context: Context? = context
    private var builderData: BuilderData<T>? = builderData
    private var dialog: ImageViewerDialog<T>? = null

    init {
        dialog = ImageViewerDialog(context, builderData)
    }

    fun show() {
        show(true)
    }

    fun show(animate: Boolean) {
        if (builderData!!.images.isNotEmpty()) {
            dialog!!.show(animate)
        } else {
            Log.w(
                context!!.getString(R.string.app_name),
                "Images list cannot be empty! Viewer ignored."
            )
        }
    }

    fun close() {
        dialog!!.close()
    }

    fun dismiss() {
        dialog!!.dismiss()
    }

    fun updateImages(images: Array<T>) {
        updateImages(ArrayList(listOf(*images)))
    }

    fun updateImages(images: List<T>) {
        if (images.isNotEmpty()) {
            dialog!!.updateImages(images)
        } else {
            dialog!!.close()
        }
    }

    fun currentPosition(): Int {
        return dialog!!.getCurrentPosition()
    }

    fun setCurrentPosition(position: Int): Int {
        return dialog!!.setCurrentPosition(position)
    }

    fun updateTransitionImage(imageView: ImageView?) {
        dialog!!.updateTransitionImage(imageView)
    }

    companion object {
        class Builder<T>(
            private val context: Context,
            images: List<T>?,
            imageLoader: ImageLoader<T>?
        ) {
            private var data: BuilderData<T>

            constructor(context: Context, images: Array<T>, imageLoader: ImageLoader<T>?) : this(
                context, ArrayList<T>(
                    listOf<T>(*images)
                ), imageLoader
            )

            constructor(
                context: Context, images: Array<T>, imageLoader: ImageLoader<T>,
                viewHolderLoader: ViewHolderLoader<T>
            ) : this(
                context,
                ArrayList<T>(listOf<T>(*images)),
                imageLoader,
                viewHolderLoader
            )

            constructor(
                context: Context, images: List<T>, imageLoader: ImageLoader<T>,
                viewHolderLoader: ViewHolderLoader<T>
            ) : this(
                context, images, imageLoader
            ) {
                data = BuilderData(images, imageLoader, viewHolderLoader)
            }

            init {
                data = BuilderData(images!!, imageLoader!!)
            }

            fun withStartPosition(position: Int): Builder<T> {
                data.startPosition = position
                return this
            }

            fun withBackgroundColor(@ColorInt color: Int): Builder<T> {
                data.backgroundColor = color
                return this
            }

            fun withBackgroundColorResource(@ColorRes color: Int): Builder<T> {
                return withBackgroundColor(ContextCompat.getColor(context, color))
            }

            fun withOverlayView(view: View?): Builder<T> {
                data.overlayView = view
                return this
            }

            fun withImagesMargin(@DimenRes dimen: Int): Builder<T> {
                data.imageMarginPixels = context.resources.getDimension(dimen).roundToInt()
                return this
            }

            fun withImageMarginPixels(marginPixels: Int): Builder<T> {
                data.imageMarginPixels = marginPixels
                return this
            }

            fun withContainerPadding(@DimenRes padding: Int): Builder<T> {
                val paddingPx = context.resources.getDimension(padding).roundToInt()
                return withContainerPaddingPixels(paddingPx, paddingPx, paddingPx, paddingPx)
            }

            fun withContainerPadding(
                @DimenRes start: Int, @DimenRes top: Int,
                @DimenRes end: Int, @DimenRes bottom: Int
            ): Builder<T> {
                withContainerPaddingPixels(
                    context.resources.getDimension(start).roundToInt(),
                    context.resources.getDimension(top).roundToInt(),
                    context.resources.getDimension(end).roundToInt(),
                    context.resources.getDimension(bottom).roundToInt()
                )
                return this
            }

            fun withContainerPaddingPixels(@Px padding: Int): Builder<T> {
                data.containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
                return this
            }

            fun withContainerPaddingPixels(
                start: Int,
                top: Int,
                end: Int,
                bottom: Int
            ): Builder<T> {
                data.containerPaddingPixels = intArrayOf(start, top, end, bottom)
                return this
            }

            fun withHiddenStatusBar(value: Boolean): Builder<T> {
                data.shouldStatusBarHide = value
                return this
            }

            fun allowZooming(value: Boolean): Builder<T> {
                data.isZoomingAllowed = value
                return this
            }

            fun allowSwipeToDismiss(value: Boolean): Builder<T> {
                data.isSwipeToDismissAllowed = value
                return this
            }

            fun withTransitionFrom(imageView: ImageView?): Builder<T> {
                data.transitionView = imageView
                return this
            }

            fun withImageChangeListener(imageChangeListener: OnImageChangeListener): Builder<T> {
                data.imageChangeListener = imageChangeListener
                return this
            }

            fun withDismissListener(onDismissListener: OnDismissListener): Builder<T> {
                data.onDismissListener = onDismissListener
                return this
            }

            fun build(): ImageViewer<T> {
                return ImageViewer(context, data)
            }

            @JvmOverloads
            fun show(animate: Boolean = true): ImageViewer<T> {
                val viewer = build()
                viewer.show(animate)
                return viewer
            }
        }
    }
}