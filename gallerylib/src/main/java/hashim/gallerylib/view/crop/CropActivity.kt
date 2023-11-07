package hashim.gallerylib.view.crop

import android.content.Intent
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ActivityCropBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.HorizontalProgressWheelView
import hashim.gallerylib.view.GalleryBaseActivity
import java.io.File

class CropActivity :
    GalleryBaseActivity(
        R.string.gallery, false, true, true, false,
        false, true, true
    ), CropViewModel.Observer {
    lateinit var binding: ActivityCropBinding
    override fun doOnCreate(arg0: Bundle?) {
        binding = putContentView(R.layout.activity_crop) as ActivityCropBinding
        binding.viewModel = ViewModelProvider(this)[CropViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
        initializeViews()
        setListener()
    }

    override fun initializeViews() {
        binding.viewModel?.position = intent.extras?.getInt("position", -1) ?: -1
        val galleryModels = intent.extras!!.get("GalleryModels") as ArrayList<GalleryModel>
        binding.viewModel?.galleryModel = galleryModels[0]
        binding.cropImageView.setImageCropOptions(binding.viewModel?.cropOptions!!)
        binding.cropImageView.setImageUriAsync(binding.viewModel?.galleryModel!!.itemUrI.toUri())
    }

    override fun updateCropOption() {
        binding.cropImageView.setImageCropOptions(binding.viewModel?.cropOptions!!)
    }

    override fun setListener() {
        binding.imgviewRotate.setOnClickListener {
            binding.cropImageView.rotateImage(90)
        }

        binding.cropImageView.setOnCropImageCompleteListener { view, result ->
            if (result.error == null) {
                val imageBitmap =
                    if (binding.cropImageView.cropShape == CropImageView.CropShape.OVAL) {
                        result.bitmap?.let(CropImage::toOvalBitmap)
                    } else {
                        result.bitmap
                    }
//                binding.cropImageView.setImageUriAsync(result.uriContent)
                if (imageBitmap == null)
                    return@setOnCropImageCompleteListener
                val path = DataProvider().saveImage(imageBitmap, this)
                val file = File(path)
                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(file.toString()), null
                ) { _, uri ->
                    runOnUiThread {
                        val newUri = uri
                            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                FileProvider.getUriForFile(
                                    this,
                                    applicationContext.packageName + ".provider",
                                    file
                                )
                            } else {
                                Uri.fromFile(file)
                            }
                        val galleryModel =
                            binding.viewModel?.getLastCroppedImage(this@CropActivity, newUri)
                        val galleryModels = ArrayList<GalleryModel>()
                        galleryModels.add(galleryModel!!)
                        val intent = Intent()
                        intent.putExtra("GalleryModels", galleryModels)
                        intent.putExtra("position", binding.viewModel?.position)
                        setResult(RESULT_OK, intent)
                        finish_activity()
                    }
                }
            }
        }
    }

    override fun onBackClicked() {
        finish_activity()
    }

    override fun finishWithSuccess() {
        binding.cropImageView.croppedImageAsync()
    }

}