package hashim.gallerylib.model

import java.io.Serializable
import java.util.Comparator

class GalleryModel : Serializable {

    lateinit var sdcardPath: String
    lateinit var itemUrI: String
    var videoDuration: Long = 0
    var videoDurationFormatted: String = "00:00"
    lateinit var name: String
    var caption = ""
    var url = ""
    var isURL: Boolean = false
    var isDeleted: Boolean = false
    var isSelected = false
    var type = ""
    var isVideo = false
    var albumName = ""
    var index_when_selected = 0
    var item_date_modified = 0
    var holderType = ""
    var columnWidth: Double = 0.0
    var columnHeight: Double = 0.0
}