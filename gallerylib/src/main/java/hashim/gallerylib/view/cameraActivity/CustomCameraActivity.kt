package hashim.gallerylib.view.cameraActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import hashim.gallerylib.R
import hashim.gallerylib.cameraCore.Camera2
import hashim.gallerylib.cameraCore.CameraPreview
import hashim.gallerylib.databinding.ActivityCustomCameraUiBinding
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.view.GalleryBaseActivity
import java.io.File

class CustomCameraActivity : GalleryBaseActivity(
    R.string.gallery, false, false, true, false,
    false, true, false
) {

    lateinit var binding: ActivityCustomCameraUiBinding

    override fun doOnCreate(arg0: Bundle?) {
        binding =
            putContentView(R.layout.activity_custom_camera_ui) as ActivityCustomCameraUiBinding
        initializeViews()
        setListener()
    }

    private lateinit var camera2: Camera2
    lateinit var mCameraPreview: CameraPreview
    lateinit var capturedBitmap: Bitmap
    lateinit var savedFile: File
    var PERMISSIONS = ArrayList<String>()

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null && permissions != null) {
            for (p in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        p
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun initializeViews() {
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
                this@CustomCameraActivity,
                PERMISSIONS.toTypedArray()
            )
        ) {
            requestPermissions(
                PERMISSIONS.toTypedArray(),
                GalleryConstants.REQUEST_Permission_Gallery
            )
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mCameraPreview = CameraPreview(this@CustomCameraActivity)
            binding.cameraPreviewPreLoliPop.addView(mCameraPreview)
        } else {
            camera2 = Camera2(this, binding.cameraView)
        }
        prepareNewImageViews()
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
            else -> {
            }
        }
    }

    private fun prepareNewImageViews() {
        runOnUiThread {
            if (::savedFile.isInitialized && savedFile != null && savedFile.exists()) {
                savedFile.delete()
                //then delete it from media store (gallery)
                DataProvider().deleteFileFromMediaStore(
                    contentResolver,
                    savedFile
                )
            }
            binding.cameraPreviewPreLoliPop.visibility = View.GONE
            binding.cameraView.visibility = View.GONE
            binding.ivImageCaptured.visibility = View.GONE
            binding.layoutDoneRetry.visibility = View.GONE

            binding.layoutFlash.visibility = View.VISIBLE
            binding.layoutCapture.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                binding.cameraPreviewPreLoliPop.visibility = View.VISIBLE
                binding.layoutFlash.visibility = View.GONE
                binding.ivSwitchCamera.visibility = View.GONE
                if (::mCameraPreview.isInitialized)
                    mCameraPreview.resumeCamera()
            } else {
                binding.cameraView.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.layoutFlash.visibility =
                        if (::camera2.isInitialized && camera2.checkSupportFlash()) View.VISIBLE else View.GONE
                }, 500)
            }
        }
    }

    private fun prepareCapturedImageViews() {
        runOnUiThread {
            binding.cameraView.visibility = View.GONE
            binding.cameraPreviewPreLoliPop.visibility = View.GONE

            binding.layoutCapture.visibility = View.GONE
            binding.cameraView.visibility = View.GONE
            binding.layoutFlash.visibility = View.GONE

            binding.ivImageCaptured.visibility = View.VISIBLE
            binding.layoutDoneRetry.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        finishWithCancel()
        super.onBackPressed()
    }

    private fun finishWithCancel() {
        if (::savedFile.isInitialized && savedFile != null && savedFile.exists()) {
            savedFile.delete()
            //then delete it from media store (gallery)
            DataProvider().deleteFileFromMediaStore(
                contentResolver,
                savedFile
            )
        }
        setResult(RESULT_CANCELED, Intent())
        finish_activity()
    }

    private fun startCapture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (::mCameraPreview.isInitialized)
                mCameraPreview.takePhoto { bitmap ->
                    //save image to check orientation
                    runOnUiThread {
                        Handler(Looper.getMainLooper()).postDelayed({
                            prepareCapturedImageViews()
                            val imagePath = DataProvider().saveImage(
                                bitmap,
                                this@CustomCameraActivity
                            )
                            capturedBitmap =
                                DataProvider().getOriginalRotationForBitmap(imagePath, bitmap)!!
                            binding.ivImageCaptured.setImageBitmap(capturedBitmap)
                            // delete the prev file
                            var tempCaptueredFile = File(imagePath)
                            if (tempCaptueredFile != null && tempCaptueredFile.exists())
                                tempCaptueredFile.delete()
                            //then delete it from media store (gallery)
                            DataProvider().deleteFileFromMediaStore(
                                contentResolver,
                                tempCaptueredFile
                            )
                            val newImagePath = DataProvider().saveImage(
                                capturedBitmap,
                                this@CustomCameraActivity
                            )
                            savedFile = File(newImagePath)
                        }, 200)
                    }
                }
        } else {
            if (::camera2.isInitialized)
                camera2.takePhoto { bitmap ->
                    runOnUiThread {
                        prepareCapturedImageViews()
                        capturedBitmap = bitmap
                        binding.ivImageCaptured.setImageBitmap(capturedBitmap)
                        val imagePath = DataProvider().saveImage(
                            bitmap,
                            this@CustomCameraActivity
                        )
                        savedFile = File(imagePath)
                    }
                }
        }
    }

    override fun setListener() {
        binding.ivBack.setOnClickListener {
            finishWithCancel()
        }

        binding.ivSwitchCamera.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (::camera2.isInitialized) {
                    camera2.switchCamera()
                    binding.layoutFlash.visibility =
                        if (camera2.checkSupportFlash()) View.VISIBLE else View.GONE
                }
            }
        }

        binding.ivCaptureImage.setOnClickListener {
            startCapture()
        }

        binding.ivDone.setOnClickListener {
            var intent = Intent()
            intent.putExtra("ImageFile", savedFile)
            setResult(RESULT_OK, intent)
            finish_activity()
        }

        binding.ivRetry.setOnClickListener {
            prepareNewImageViews()
        }

        binding.ivCameraFlashOn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (::camera2.isInitialized)
                    camera2.setFlash(Camera2.FLASH.ON)
                it.alpha = 1f
                binding.ivCameraFlashAuto.alpha = 0.4f
                binding.ivCameraFlashOff.alpha = 0.4f
            }
        }

        binding.ivCameraFlashAuto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.ivCameraFlashOff.alpha = 0.4f
                binding.ivCameraFlashOn.alpha = 0.4f
                it.alpha = 1f
                if (::camera2.isInitialized)
                    camera2.setFlash(Camera2.FLASH.AUTO)
            }
        }

        binding.ivCameraFlashOff.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (::camera2.isInitialized)
                    camera2.setFlash(Camera2.FLASH.OFF)
                it.alpha = 1f
                binding.ivCameraFlashOn.alpha = 0.4f
                binding.ivCameraFlashAuto.alpha = 0.4f
            }
        }
    }

    override fun onPause() {
        //  cameraPreview.pauseCamera()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        } else {
            if (::camera2.isInitialized)
                camera2.close()
        }
        super.onPause()
    }

    override fun onResume() {
        // cameraPreview.resumeCamera()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (::mCameraPreview.isInitialized)
                mCameraPreview.resumeCamera()
        } else {
            if (::camera2.isInitialized)
                camera2.onResume()
        }
        prepareNewImageViews()
        super.onResume()
    }

}
