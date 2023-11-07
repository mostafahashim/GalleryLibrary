package hashim.gallerylib.view.selected

import androidx.lifecycle.ViewModel
import hashim.gallerylib.model.GalleryModel

class SelectedViewModel : ViewModel() {
    lateinit var observer: Observer
    lateinit var selectedPagerAdapter: RecyclerSelectedPagerAdapter
    var selectedPhotos: ArrayList<GalleryModel> = ArrayList()

    interface Observer {
        fun cropImage()
        fun onBackClicked()
        fun finishWithSuccess()
    }
}