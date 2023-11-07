package hashim.gallerylib.view.crop

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.RadioGroup
import androidx.lifecycle.ViewModel
import com.canhub.cropper.CropImageOptions
import hashim.gallerylib.R
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants

class CropViewModel : ViewModel() {
    lateinit var observer: Observer
    var galleryModel: GalleryModel = GalleryModel()
    var position = -1
    var cropOptions: CropImageOptions? = CropImageOptions()

    fun onRadioChanged(radioGroup: RadioGroup, id: Int) {
        when (id) {
            R.id.radioFree -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = false, aspectRatioX = 1, aspectRatioY = 1)
            }

            R.id.radioOneOne -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 1, aspectRatioY = 1)
            }

            R.id.radioTwoOne -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 2, aspectRatioY = 1)
            }

            R.id.radioOneTwo -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 1, aspectRatioY = 2)
            }

            R.id.radioThreeFour -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 3, aspectRatioY = 4)
            }

            R.id.radioFourThree -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 4, aspectRatioY = 3)
            }

            R.id.radioNineSixteen -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 9, aspectRatioY = 16)
            }

            R.id.radioSixteenNine -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 16, aspectRatioY = 9)
            }

            R.id.radioTwoThree -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 2, aspectRatioY = 3)
            }

            R.id.radioThreeTwo -> {
                cropOptions =
                    cropOptions?.copy(fixAspectRatio = true, aspectRatioX = 3, aspectRatioY = 2)
            }
        }
        observer.updateCropOption()
    }

    fun flipHorizontal() {
        cropOptions = cropOptions?.copy(flipHorizontally = !cropOptions?.flipHorizontally!!)
        observer.updateCropOption()
    }

    fun flipVertical() {
        cropOptions = cropOptions?.copy(flipVertically = !cropOptions?.flipVertically!!)
        observer.updateCropOption()
    }

    @SuppressLint("Range")
    fun getLastCroppedImage(context: Context, uri: Uri): GalleryModel? {
        try {
            val columnsImages = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_ADDED,
                "bucket_id",
                "bucket_display_name",
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID
            )
            val orderByimage = MediaStore.Images.Media.DATE_MODIFIED
            val imagecursor = context.contentResolver.query(
                uri,
                columnsImages, null, null, "$orderByimage DESC"
            )
            if (imagecursor != null && imagecursor.count > 0) {
                imagecursor.moveToFirst()
                val item = GalleryModel()
                item.index_when_selected = 1
                item.isSelected = true
                // get path
                val dataColumnIndex = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.DATA)
                item.sdcardPath = imagecursor.getString(dataColumnIndex)
                // get date modified
                var dateColumnIndex = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                var Image_date = imagecursor.getString(dateColumnIndex)
                if (Image_date != null) {
                    item.item_date_modified = Integer.parseInt(Image_date)
                } else {
                    dateColumnIndex = imagecursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    Image_date = imagecursor.getString(dateColumnIndex)
                    if (Image_date != null) {
                        item.item_date_modified = Integer.parseInt(Image_date)
                    } else {
                        item.item_date_modified = SystemClock.elapsedRealtime().toInt()
                    }
                }
                //get Name
                val nameColumnIndex = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val name = imagecursor.getString(nameColumnIndex)
                if (name != null) {
                    item.name = name
                } else {
                    item.name = "unknown"
                }
                // get uri
                // content://media/external/images/media/19490
                val imageuri = ContentUris
                    .withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imagecursor.getInt(
                            imagecursor
                                .getColumnIndex(MediaStore.Images.Media._ID)
                        ).toLong()
                    )
                item.itemUrI = imageuri.toString()
                item.type = GalleryConstants.GalleryTypeImages
                item.url = "file://" + item.sdcardPath
                item.isVideo = false
                //get albumName
                val albumNameColumnIndex = imagecursor
                    .getColumnIndex("bucket_display_name")
                item.albumName = imagecursor.getString(albumNameColumnIndex)
                //add its album
                imagecursor.close()
                return item
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    interface Observer {
        fun onBackClicked()
        fun finishWithSuccess()
        fun updateCropOption()
    }
}