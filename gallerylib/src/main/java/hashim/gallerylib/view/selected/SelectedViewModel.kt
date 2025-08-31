package hashim.gallerylib.view.selected

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hashim.gallerylib.adapter.RecyclerSelectedThumbnailsAdapter
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnItemThumbnailListener

class SelectedViewModel : ViewModel() {
    lateinit var observer: Observer
    lateinit var selectedPagerAdapter: RecyclerSelectedPagerAdapter
    var selectedPhotos: ArrayList<GalleryModel> = ArrayList()
    lateinit var recyclerSelectedThumbnailsAdapter: RecyclerSelectedThumbnailsAdapter
    var isItemsMoreThanOne = MutableLiveData(false)

    fun initThumbnailsAdapter(screenWidth: Int) {
        val columnWidth = (50.00 * screenWidth) / 360.00
        recyclerSelectedThumbnailsAdapter = RecyclerSelectedThumbnailsAdapter(
            columnWidth,
            selectedPhotos, object : OnItemThumbnailListener {
                override fun onItemSelectedListener(position: Int) {
                    selectOnlyItem(position)
                }

                override fun onItemDeleted(position: Int) {
                    deleteItem(position)
                }
            }
        )
    }

    fun selectOnlyItem(position: Int, isFromPager: Boolean = false) {
        if (position < 0 || position >= selectedPhotos.size) return
        // 1. get current selected
        val oldIndex = selectedPhotos.indexOfFirst { it.isSelected }

        // 2.unselect it
        if (oldIndex != -1) {
            selectedPhotos[oldIndex].isSelected = false
            recyclerSelectedThumbnailsAdapter.notifyItemChanged(oldIndex)
        }

        // 3. select position
        selectedPhotos[position].isSelected = true
        recyclerSelectedThumbnailsAdapter.notifyItemChanged(position)

        if (!isFromPager) {
            observer.animateToPagerPosition(position)
        } else {
            observer.animateRecyclerViewToPosition(position)
        }
    }

    fun deleteItem(position: Int) {
        if (position < 0 || position >= selectedPhotos.size) return

        // 1. delete item
        selectedPhotos.removeAt(position)
        recyclerSelectedThumbnailsAdapter.notifyItemRemoved(position)
        selectedPagerAdapter.notifyItemRemoved(position)

        // 2. update list count
        isItemsMoreThanOne.value = selectedPhotos.size > 1

        // 3.check if last item
        if (selectedPhotos.isNotEmpty()) {
            val newPosition = when {
                position < selectedPhotos.size -> position    // choose next position
                else -> selectedPhotos.lastIndex               //choose last index if last index
            }

            val currentPagerPos = observer.getCurrentPagerPosition()

            // select new position and don't animate pager if current position
            selectOnlyItem(newPosition, isFromPager = currentPagerPos == newPosition)
        }
    }


    interface Observer {
        fun cropImage()
        fun onBackClicked()
        fun finishWithSuccess()
        fun animateToPagerPosition(position: Int)
        fun getCurrentPagerPosition(): Int
        fun animateRecyclerViewToPosition(position: Int)
    }
}