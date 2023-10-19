package hashim.gallerylib.imageviewer.viewer.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import hashim.gallerylib.imageviewer.common.extensions.*
import hashim.gallerylib.util.ScreenSizeUtils
import hashim.gallerylib.util.activity

internal class TransitionImageAnimator(
    private val externalImage: ImageView?,
    private val internalImage: ImageView,
    private val internalImageContainer: FrameLayout
) {

    companion object {
                private const val TRANSITION_DURATION_OPEN = 200L
        private const val TRANSITION_DURATION_CLOSE = 250L
//        private const val TRANSITION_DURATION_OPEN = 5000L
//        private const val TRANSITION_DURATION_CLOSE = 5000L
    }

    internal var isAnimating = false

    private var isClosing = false

    private val transitionDuration: Long
        get() = if (isClosing) TRANSITION_DURATION_CLOSE else TRANSITION_DURATION_OPEN

    private val internalRoot: ViewGroup
        get() = internalImageContainer.parent as ViewGroup

    internal fun animateOpen(
        containerPadding: IntArray,
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible) {
            onTransitionStart(TRANSITION_DURATION_OPEN)
            doOpenTransition(containerPadding, onTransitionEnd)
        } else {
            onTransitionEnd()
        }
    }

    internal fun animateClose(
        shouldDismissToBottom: Boolean,
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible && !shouldDismissToBottom) {
            onTransitionStart(TRANSITION_DURATION_CLOSE)
            doCloseTransition(onTransitionEnd)
        } else {
            externalImage?.visibility = View.VISIBLE
            onTransitionEnd()
        }
    }

    private fun doOpenTransition(containerPadding: IntArray, onTransitionEnd: () -> Unit) {
        isAnimating = true
        prepareTransitionLayout()

        internalRoot.postApply {
            //ain't nothing but a kludge to prevent blinking when transition is starting
            externalImage?.postDelayed(50) { visibility = View.INVISIBLE }

            TransitionManager.beginDelayedTransition(internalRoot, createTransition {
                if (!isClosing) {
                    isAnimating = false
                    onTransitionEnd()
                }
            })

            internalImageContainer.makeViewMatchParent()
            internalImage.makeViewMatchParent()

            internalRoot.applyMargin(
                containerPadding[0],
                containerPadding[1],
                containerPadding[2],
                containerPadding[3]
            )

            internalImageContainer.requestLayout()
        }
    }

    private fun doCloseTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        isClosing = true

        TransitionManager.beginDelayedTransition(
            internalRoot, createTransition { handleCloseTransitionEnd(onTransitionEnd) })

        prepareTransitionLayout()
        internalImageContainer.requestLayout()
    }

    fun getScreenWidth(context: Context): Int {
        return try {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        } catch (e: Exception) {
            0
        }
    }

    private fun prepareTransitionLayout() {
        externalImage?.let {
            if (externalImage.isRectVisible) {
                with(externalImage.localVisibleRect) {
                    internalImage.requestNewSize(it.width, it.height)
                    val isRTl = it.layoutDirection == View.LAYOUT_DIRECTION_RTL
                    if (isRTl) {
                        internalImage.applyMargin(top = -top, start = -right, end = right)
                    } else {
                        internalImage.applyMargin(top = -top, start = -left)
                    }
                }
                with(externalImage.globalVisibleRect) {
                    internalImageContainer.requestNewSize(width(), height())
                    val isRTl = it.layoutDirection == View.LAYOUT_DIRECTION_RTL
                    if (isRTl) {
                        val start = getScreenWidth(it.context) - left
                        val end = getScreenWidth(it.context) - right
                        internalImageContainer.applyMargin(start, top, end, bottom)
                    } else {
                        internalImageContainer.applyMargin(left, top, right, bottom)
                    }


                }
            }

            resetRootTranslation()
        }
    }


    private fun handleCloseTransitionEnd(onTransitionEnd: () -> Unit) {
        externalImage?.visibility = View.VISIBLE
        internalImage.post { onTransitionEnd() }
        isAnimating = false
    }

    private fun resetRootTranslation() {
        internalRoot
            .animate()
            .translationY(0f)
            .setDuration(transitionDuration)
            .start()
    }

    private fun createTransition(onTransitionEnd: (() -> Unit)? = null): Transition =
        AutoTransition()
            .setDuration(transitionDuration)
            .setInterpolator(DecelerateInterpolator())
            .addListener(onTransitionEnd = { onTransitionEnd?.invoke() })
}