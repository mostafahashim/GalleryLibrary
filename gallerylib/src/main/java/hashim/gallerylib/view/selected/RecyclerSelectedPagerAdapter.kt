package hashim.gallerylib.view.selected

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.recyclerview.widget.RecyclerView
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemPagerSelectedBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants

class RecyclerSelectedPagerAdapter(
    val models: ArrayList<GalleryModel>,
) : RecyclerView.Adapter<RecyclerSelectedPagerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_pager_selected,
            parent,
            false
        ) as ItemPagerSelectedBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = models.size


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = models[holder.layoutPosition]

        holder.binding.model = item
        if (item.type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
            if (item.isReleasePlayer) {
                models[holder.layoutPosition].player?.release()
            } else {
                initVideoPlayer(item, holder)
            }

        }
        holder.itemView.setOnClickListener {
        }

    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun initVideoPlayer(model: GalleryModel, holder: ViewHolder) {
        model.player =
            ExoPlayer.Builder(holder.binding.videoView.context).build().also { exoPlayer ->
                holder.binding.videoView.player = exoPlayer
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
                holder.binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                //player seekbar controller
                holder.binding.videoView.useController = true
                exoPlayer.volume = 1.0f
                holder.binding.videoView.setShutterBackgroundColor(Color.BLACK)
                exoPlayer.prepare()
            }
    }

//    override fun onViewRecycled(holder: ViewHolder) {
//        if (models[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0 && models[holder.layoutPosition].player != null) {
//            models[holder.layoutPosition].player!!.release()
//            models[holder.layoutPosition].player = null
//        }
//        super.onViewRecycled(holder)
//    }

//    override fun onViewDetachedFromWindow(holder: ViewHolder) {
//        if (models[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0 && models[holder.layoutPosition].player != null) {
//            models[holder.layoutPosition].player!!.stop()
//            models[holder.layoutPosition].player!!.release()
//        }
//        super.onViewDetachedFromWindow(holder)
//    }
//
//    override fun onViewAttachedToWindow(holder: ViewHolder) {
//        if (models[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
//            initVideoPlayer(models[holder.layoutPosition], holder)
//        }
//        super.onViewAttachedToWindow(holder)
//    }

    class ViewHolder(var binding: ItemPagerSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }


}