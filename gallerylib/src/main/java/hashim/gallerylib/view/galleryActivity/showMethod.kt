package hashim.gallerylib.view.galleryActivity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnBottomSheetItemClickListener
import hashim.gallerylib.observer.OnResultCallback
import hashim.gallerylib.util.GalleryConstants

class GalleryLib(var myActivity: AppCompatActivity) {
    lateinit var myOnResultCallback: OnResultCallback
    fun showGallery(
        isDialog: Boolean,
        selectionType: String,
        locale: String,
        maxSelectionCount: Int,
        gridColumnsCount: Int,
        selected: ArrayList<GalleryModel>,
        onResultCallback: OnResultCallback?,
        galleryResultLauncher: ActivityResultLauncher<Intent>?
    ) {
        if (isDialog && onResultCallback != null) {
            myOnResultCallback = onResultCallback
            val bottomSheetFragment = BottomSheetGalleryFragment()
            val bundle = Bundle()
            bundle.putSerializable(GalleryConstants.selected, selected)
            bundle.putSerializable(GalleryConstants.maxSelectionCount, maxSelectionCount)
            bundle.putSerializable(GalleryConstants.gridColumnsCount, gridColumnsCount)
            bundle.putSerializable(GalleryConstants.showType, selectionType)
            bundle.putSerializable(GalleryConstants.Language, locale)

            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.onResultCallback = object : OnResultCallback {
                override fun onResult(list: ArrayList<GalleryModel>) {
                    onResultCallback.onResult(list)
                }

                override fun onDismiss() {
                    onResultCallback.onDismiss()
                }
            }
            bottomSheetFragment.show(myActivity.supportFragmentManager, bottomSheetFragment.tag)
        } else {
            if (galleryResultLauncher == null)
                return
            Intent(myActivity, GalleryActivity::class.java).also {
                it.putExtra(GalleryConstants.maxSelectionCount, maxSelectionCount)
                it.putExtra(GalleryConstants.showType, selectionType)
                it.putExtra(GalleryConstants.selected, selected)
                it.putExtra(GalleryConstants.Language, locale)
                it.putExtra(GalleryConstants.gridColumnsCount, gridColumnsCount)

                galleryResultLauncher.launch(it)
            }
        }
    }

}