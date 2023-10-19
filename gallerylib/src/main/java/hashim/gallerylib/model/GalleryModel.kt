package hashim.gallerylib.model

import androidx.media3.exoplayer.ExoPlayer
import java.io.Serializable
import java.util.Comparator

data class GalleryModel(
    var sdcardPath: String = "",
    var itemUrI: String = "",
    var videoDuration: Long = 0,
    var videoDurationFormatted: String = "00:00",
    var name: String = "",
    var caption: String = "",
    var url: String = "",
    var isURL: Boolean = false,
    var isDeleted: Boolean = false,
    var isSelected: Boolean = false,
    var type: String = "",
    var isVideo: Boolean = false,
    var albumName: String = "",
    var index_when_selected: Int = 0,
    var item_date_modified: Int = 0,
    var holderType: String = "",
    var columnWidth: Double = 0.0,
    var columnHeight: Double = 0.0,
) : Serializable {
    var player: ExoPlayer? = null

}