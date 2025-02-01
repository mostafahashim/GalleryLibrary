package hashim.gallerylib.view.cameraActivity

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hashim.gallerylib.R
import hashim.gallerylib.adapter.RecyclerGalleryAdapter
import hashim.gallerylib.cameraCore.Camera2
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnItemSelectedListener
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ScreenSizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class CameraViewModel : ViewModel() {
    lateinit var observer: Observer
    var capturedBitmap: MutableLiveData<Bitmap>? = MutableLiveData(null)
    var savedFile: File? = null
    var camera2: Camera2? = null


    //0 = OFF, 1 = On, 2 = Auto
    val flashType = MutableLiveData(0)

    var isShowFlash = MutableLiveData(false)
    var isShowCameraView = MutableLiveData(false)
    var isShowCapture = MutableLiveData(false)
    var isShowImageCaptured = MutableLiveData(false)
    var isShowDoneRetry = MutableLiveData(false)


    fun prepareNewImageViews() {
        isShowCameraView.postValue(false)
        isShowImageCaptured.postValue(false)
        isShowDoneRetry.postValue(false)

        isShowFlash.postValue(true)
        isShowCapture.postValue(true)
        isShowCameraView.postValue(true)

        isShowFlash.value = camera2?.checkSupportFlash()!!
    }

    fun setFlashType(type: Int) {
        flashType.postValue(type)
        when (type) {
            0 -> {
                camera2?.setFlash(Camera2.FLASH.OFF)
            }

            1 -> {
                camera2?.setFlash(Camera2.FLASH.ON)
            }

            2 -> {
                camera2?.setFlash(Camera2.FLASH.AUTO)
            }
        }
    }

    fun switchCamera() {
        camera2?.switchCamera()
        isShowFlash.value = camera2?.checkSupportFlash()!!
    }

    fun startCapture() {
        camera2?.takePhoto { bitmap ->
            prepareCapturedImageViews()
            capturedBitmap?.postValue(bitmap)
        }
    }

    private fun prepareCapturedImageViews() {
        isShowCapture.postValue(false)
        isShowCameraView.postValue(false)
        isShowFlash.postValue(false)

        isShowImageCaptured.postValue(true)
        isShowDoneRetry.postValue(true)
    }

    fun saveCapturedFile() {
        observer.showLoader(true)
        observer.saveImage()
    }

    interface Observer {
        fun showLoader(isShow: Boolean)
        fun finishWithCancel()
        fun finishWithSuccess()
        fun saveImage()
    }
}