package hashim.gallerylib.model


class ComparableGalleryModel {

    object Holder {
        val INSTANCE = ComparableGalleryModel()
    }

    companion object {
        val instance =
            Holder.INSTANCE
    }

    var compareIndex_when_selected: java.util.Comparator<GalleryModel> = Comparator { o1, o2 ->
        // sort descending
        o1.index_when_selected - o2.index_when_selected
    }

    var compareDateModified: Comparator<GalleryModel> = Comparator { o1, o2 ->
        // sort descending
        o1.item_date_modified - o2.item_date_modified
    }
}