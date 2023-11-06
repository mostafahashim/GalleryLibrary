package hashim.gallerylib.model

import androidx.media3.exoplayer.ExoPlayer
import java.io.Serializable
import java.util.Comparator

data class AlbumModel(
    var name: String = "",
    var mainImage: String = "",
    var imagesCount: Int = 0,
    var isSelected: Boolean = false,
    var holderType: String = "",
    var columnWidth: Double = 0.0,
    var columnHeight: Double = 0.0,
) : Serializable {

}