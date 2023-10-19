package hashim.gallerylib.view.galleryActivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.bumptech.glide.Glide
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemPagerGalleryBinding
import hashim.gallerylib.imageviewer.viewer.viewholder.DefaultViewHolder
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants

class GalleryPagerViewHolder<T> constructor(
    parent: View,
    var imageView: ImageView,
    var binding: ItemPagerGalleryBinding,
) : DefaultViewHolder<T>(
    parent
) {
    private var active = false
    private var haveVideo = false

    companion object {
        fun buildViewHolder(
            imageView: ImageView
        ): GalleryPagerViewHolder<GalleryModel> {
            val inflater = LayoutInflater.from(imageView.context)
            val parent = FrameLayout(imageView.context)
            val binding = DataBindingUtil.inflate(
                inflater, R.layout.item_pager_gallery, parent, false
            ) as ItemPagerGalleryBinding
            parent.addView(binding.root)
            parent.addView(imageView)
            return GalleryPagerViewHolder(parent, imageView, binding)
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun initVideoPlayer(model: GalleryModel) {
        model.player = ExoPlayer.Builder(binding.videoView.context).build().also { exoPlayer ->
            binding.videoView.player = exoPlayer
            exoPlayer.setMediaItem(
                MediaItem.fromUri(model.itemUrI)
            )
            exoPlayer.playWhenReady = false
            exoPlayer.addListener(object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> {}
                        Player.STATE_BUFFERING -> {}
                        Player.STATE_READY -> {
                        }

                        Player.STATE_ENDED -> {
                        }
                    }
                }
            })
            val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build()
            exoPlayer.setAudioAttributes(audioAttributes, false)
//                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
            //This will increase video's height or width to fit with maintaining aspect ratios of video.
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            //player seekbar controller
            binding.videoView.useController = true
            exoPlayer.volume = 1.0f
            binding.videoView.setShutterBackgroundColor(Color.BLACK)
            exoPlayer.prepare()
        }
    }

    override fun bind(position: Int, model: T) {
        val galleryModel = model as GalleryModel
        if (model.player != null) model.player?.playWhenReady = false
        if (galleryModel.type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
            haveVideo = true
            imageView.visibility = View.GONE
            binding.ivImage.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            initVideoPlayer(galleryModel)
        } else {
            haveVideo = false
            imageView.visibility = View.VISIBLE
            binding.ivImage.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            Glide.with(imageView.context).load(galleryModel.url).into(imageView)
        }
    }

    override fun onDialogClosed(position: Int, model: T) {
        // The VideoView must be stopped and hidden as the dialog begins to close,
        // or we'll keep seeing the video under the animation due to how SurfaceViews work.
        // But by delaying that, we avoid some flicker as the close animation starts.
        binding.videoView.postDelayed({
            binding.videoView.visibility = View.GONE
            model as GalleryModel
            if (model.player != null) model.player?.release()
            model.player = null
        }, 10)
    }

    override fun destroyItem(parent: ViewGroup, position: Int, model:T) {
        model as GalleryModel
        if (model.player != null)
            model.player?.release()
        model.player = null
    }

    override fun setIsVisible(isVisible: Boolean, position: Int, model: T) {
        if (isVisible == active)
            return
        active = isVisible
        if (!haveVideo) return
        model as GalleryModel
        // Pause videos as they scroll off-screen, and play videos as they scroll on-screen
        if (!isVisible) {
            if (model.player != null)
                model.player?.pause()
        } else {
//            if (player != null) player?.release()
        }
    }
}