package hashim.gallerylib.view.selected

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemPagerSelectedBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.view.crop.CropActivity

class SelectedPageFragment : Fragment() {

    private lateinit var galleryModel: GalleryModel

    lateinit var binding: ItemPagerSelectedBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.item_pager_selected, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryModel = requireArguments().getSerializable("GalleryModel") as GalleryModel
        binding.model = galleryModel
    }

    override fun onResume() {
        super.onResume()
        if (galleryModel.type.compareTo(GalleryConstants.GalleryTypeVideos) == 0)
            initVideoPlayer(galleryModel)
    }

    override fun onPause() {
        super.onPause()

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

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onDetach() {
        super.onDetach()
//        if (galleryModel.player != null)
//            galleryModel.player?.release()
//        galleryModel.player = null
    }
}