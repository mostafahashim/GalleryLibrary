package hashim.gallerylib.view.cameraActivity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProvider
import hashim.gallerylib.R
import hashim.gallerylib.cameraCore.Camera2
import hashim.gallerylib.databinding.ActivityCustomCameraUiBinding
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ProgressLoading
import hashim.gallerylib.view.GalleryBaseActivity

class CameraActivity : GalleryBaseActivity(
    R.string.gallery, false, false, true, false,
    false, true, false
), CameraViewModel.Observer {

    lateinit var binding: ActivityCustomCameraUiBinding

    override fun doOnCreate(arg0: Bundle?) {
        binding =
            putContentView(R.layout.activity_custom_camera_ui) as ActivityCustomCameraUiBinding
        binding.viewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
        initializeViews()
        setListener()
    }

    override fun setListener() {
    }

    override fun initializeViews() {
        if (!checkPermissions())
            return
        binding.viewModel?.camera2 = Camera2(this, binding.cameraView)
        binding.viewModel?.prepareNewImageViews()
    }

    var PERMISSIONS = ArrayList<String>()

    fun checkPermissions(): Boolean {
        PERMISSIONS = ArrayList()
        PERMISSIONS.add(Manifest.permission.CAMERA)
        PERMISSIONS.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_IMAGES)
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_VIDEO)
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= 23 && !hasPermissions(
                this@CameraActivity,
                PERMISSIONS.toTypedArray()
            )
        ) {
            requestPermissions(
                PERMISSIONS.toTypedArray(),
                GalleryConstants.REQUEST_Permission_Gallery
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GalleryConstants.REQUEST_Permission_Gallery -> {
                // If request is cancelled, the result arrays are empty.
                initializeViews()
            }
        }
    }

    override fun finishWithCancel() {
        finish_activity()
    }

    override fun finishWithSuccess() {
        Intent().also {
            it.putExtra("ImageFile", binding.viewModel?.savedFile)
            setResult(RESULT_OK, it)
            finish_activity()
        }
    }

    override fun showLoader(isShow: Boolean) {
        if (isShow)
            ProgressLoading.show(this)
        else
            ProgressLoading.dismiss(this)
    }

    override fun onPause() {
        binding.viewModel?.camera2?.close()
        super.onPause()
    }

    override fun onResume() {
        binding.viewModel?.camera2?.onResume()
        binding.viewModel?.prepareNewImageViews()
        super.onResume()
    }

    override fun saveImage(): String {
        return DataProvider().saveImage(binding.viewModel?.capturedBitmap?.value!!, this)
    }

}
