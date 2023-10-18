package hashim.gallerylib.view.galleryActivity

import android.media.MediaPlayer
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.bumptech.glide.Glide
import hashim.gallerylib.R
import hashim.gallerylib.imageviewer.viewer.viewholder.DefaultViewHolder
import hashim.gallerylib.model.GalleryModel


class CustomViewHolder<T> constructor(
    parentView: View,
    iv: ImageView,
    vv: VideoView,
    tv: TextView
) : DefaultViewHolder<T>(
    parentView
) {
    private var imageView: ImageView? = iv
    private var videoView: VideoView? = vv
    private var textView: TextView? = tv
    private var active = false
    private var haveVideo = false

    companion object {
        fun buildViewHolder(imageView: ImageView): CustomViewHolder<GalleryModel> {
            val parent = FrameLayout(imageView.context)
            val textView = TextView(imageView.context)
            val videoView = VideoView(imageView.context)
            textView.setTextColor(imageView.context.resources.getColor(R.color.colorPrimary))
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.setPadding(0, 100, 0, 0)
            textView.textSize = 32f
            parent.addView(videoView)
//            parent.removeView(imageView)
//            parent.addView(imageView)
            parent.addView(textView)
            val params = videoView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.CENTER
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            videoView.layoutParams = params
            return CustomViewHolder(parent, imageView, videoView, textView)
        }
    }

    override fun bind(position: Int, uri: T) {
        val src = uri.toString()
        if (videoView!!.isPlaying) videoView!!.stopPlayback()
        textView!!.text = src.substring(src.lastIndexOf("/") + 1)
        if (src.contains("video")) {
            haveVideo = true
            imageView!!.visibility = View.GONE
            videoView!!.setVideoURI(Uri.parse(src))
            videoView!!.setOnPreparedListener { mediaPlayer: MediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.start()
                if (!active) videoView!!.pause()
                // Show videoView here to avoid flicker after the open transition
                videoView!!.visibility = View.VISIBLE
            }
            videoView!!.start()
        } else {
            haveVideo = false
            imageView!!.visibility = View.VISIBLE
            videoView!!.visibility = View.GONE
            Glide.with(imageView!!.context).load(uri).into(imageView!!)
        }
    }

    override fun onDialogClosed() {
        // The VideoView must be stopped and hidden as the dialog begins to close,
        // or we'll keep seeing the video under the animation due to how SurfaceViews work.
        // But by delaying that, we avoid some flicker as the close animation starts.
        videoView!!.postDelayed({
            videoView!!.visibility = View.GONE
            if (videoView!!.isPlaying) videoView!!.stopPlayback()
        }, 10)
    }

    override fun setIsVisible(isVisible: Boolean) {
        if (isVisible == active) return
        active = isVisible
        if (!haveVideo) return

        // Pause videos as they scroll off-screen, and play videos as they scroll on-screen
        if (isVisible) videoView!!.resume() else {
            videoView!!.pause()
            videoView!!.seekTo(0)
        }
    }
}