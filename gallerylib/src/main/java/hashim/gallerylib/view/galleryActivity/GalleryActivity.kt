package hashim.gallerylib.view.galleryActivity


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ActivityGalleryBinding
import hashim.gallerylib.model.ComparableGalleryModel
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnBottomSheetItemClickListener
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ScreenSizeUtils
import hashim.gallerylib.view.CustomCameraActivity
import hashim.gallerylib.view.GalleryBaseActivity
import hashim.gallerylib.view.sub.BottomSheetAlbumsFragment
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class GalleryActivity : GalleryBaseActivity(
    R.string.gallery, false, true, true, false,
    false, true, false
), GalleryViewModel.Observer {

    lateinit var binding: ActivityGalleryBinding

    override fun doOnCreate(arg0: Bundle?) {
        binding = putContentView(R.layout.activity_gallery) as ActivityGalleryBinding
        binding.viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
        initializeViews()
        setListener()
    }

    @Suppress("UNCHECKED_CAST")
    override fun initializeViews() {
        binding.viewModel?.selectedPhotos =
            intent.extras!!.get(GalleryConstants.selected) as ArrayList<GalleryModel>
        binding.viewModel?.maxSelectionCount =
            intent.getIntExtra(GalleryConstants.maxSelectionCount, 50)
        binding.viewModel?.showType =
            intent.getStringExtra(GalleryConstants.showType) ?: GalleryConstants.GalleryTypeImages

        binding.viewModel?.updateBooksAdapterColumnWidth(
            (115.00 * ScreenSizeUtils().getScreenWidth(
                this
            )) / 360.00
        )
        binding.viewModel?.initializeCameraButtons()
        binding.viewModel?.initGalleryAdapter()
        fetchData()
    }

    override fun setListener() {
    }

    override fun openFinish() {
        binding.viewModel?.selectedPhotos =
            binding.viewModel?.recyclerGalleryAdapter?.getSelected() ?: ArrayList()
        getIntentForSelectedItems(binding.viewModel?.selectedPhotos)
        finish_activity()
    }

    override fun openAlbums() {
        val bottomSheetFragment = BottomSheetAlbumsFragment()
        val bundle = Bundle()

        bundle.putSerializable(
            "AlbumModels", binding.viewModel?.folderModels
        )
        bundle.putSerializable("title", getString(R.string.choose_album))
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.setOnBottomSheetItemClickObserver(object :
            OnBottomSheetItemClickListener {
            override fun onBottomSheetItemClickListener(position: Int) {
                binding.viewModel?.selectedAlbumName?.value =
                    binding.viewModel?.folderModels!![position]
                binding.viewModel?.recyclerGalleryAdapter?.filter(
                    if (position == 0) ""
                    else binding.viewModel?.folderModels!![position]
                )
                binding.rcGallery.scrollToPosition(0)
            }
        })
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun onBackClicked() {
        binding.viewModel?.selectedPhotos =
            binding.viewModel?.recyclerGalleryAdapter?.getSelected() ?: ArrayList()
        finish_activity()
    }


    private fun fetchData() {
        if (checkPermissions())
            Handler(Looper.getMainLooper()).post {
                binding.viewModel?.recyclerGalleryAdapter?.addAll(
                    binding.viewModel?.getGalleryPhotosAndVideos(
                        this as Context
                    ) ?: ArrayList()
                )
                checkImageStatus()
            }
    }

    var PERMISSIONS = ArrayList<String>()
    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
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
                this@GalleryActivity,
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

    override fun onResume() {
        super.onResume()
    }

    private fun checkImageStatus() {
        if (binding.viewModel?.recyclerGalleryAdapter?.itemCount == 0) {
            //no media found
            binding.viewModel?.isHasMedia?.value = false
        } else {
            //media found
            binding.viewModel?.isHasMedia?.value = true
            //select album if one item selected, else select all albums
            if (binding.viewModel?.selectedPhotos != null && binding.viewModel?.selectedPhotos?.size!! == 1) {
                binding.viewModel?.recyclerGalleryAdapter?.filter(binding.viewModel?.selectedPhotos!![0].albumName)
                //change album name in dropdown
                binding.viewModel?.selectedAlbumName?.value =
                    binding.viewModel?.selectedPhotos!![0].albumName
            } else {
                binding.viewModel?.selectedAlbumName?.value = getString(R.string.all)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GalleryConstants.REQUEST_Permission_Gallery ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchData()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.you_should_allow_all_permissions_to_fetch_gallery_images))
                        .setPositiveButton(getString(R.string.settings)) { dialog, which ->
                            // Respond to positive button press
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                packageName, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                            // Respond to positive button press
                        }
                        .show()
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            else -> {
            }
        }
    }

    override fun captureVideo() {
        if (!checkPermissions())
            return
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        binding.viewModel?.tempCaptueredFile =
            DataProvider()
                .getFile(GalleryConstants.TYPE_VIDEO_INTERNAL, applicationContext)
        val videoTimeInMinutes = 1 * 60
        intent.putExtra(
            MediaStore.EXTRA_DURATION_LIMIT,
            videoTimeInMinutes
        )

        val mVideoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                binding.viewModel?.tempCaptueredFile!!
            )
        } else {
            Uri.fromFile(binding.viewModel?.tempCaptueredFile)
        }

        intent.putExtra(
            MediaStore.EXTRA_OUTPUT, mVideoUri
        )
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        videoResultLauncher.launch(intent)
    }

    var videoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && binding.viewModel?.tempCaptueredFile != null) {
                try {
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(binding.viewModel?.tempCaptueredFile.toString()), null
                    ) { path, uri ->
                        runOnUiThread {
                            var newUri = Uri.parse("")
                            if (uri != null) {
                                newUri = uri!!
                            } else if (result.data != null && result.data?.data != null) {
                                newUri = result.data?.data!!
                            } else {
//                            newUri = if (Build.VERSION.SDK_INT < 24) {
//                                Uri.fromFile(tempCaptueredFile)
//                            } else {
//                                Uri.parse(tempCaptueredFile.path) // My work-around for new SDKs, doesn't work in Android 10.
//                            }

                                newUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    FileProvider.getUriForFile(
                                        this,
                                        applicationContext.packageName + ".provider",
                                        binding.viewModel?.tempCaptueredFile!!
                                    )
                                } else {
                                    Uri.fromFile(binding.viewModel?.tempCaptueredFile)
                                }
                            }
                            val galleryModel =
                                binding.viewModel?.getLastCapturedGalleryVideo(this, newUri)
                            if (galleryModel != null) {
                                binding.viewModel?.selectedAlbumName?.value =
                                    getString(R.string.all)
                                binding.viewModel?.recyclerGalleryAdapter?.filter("")
                                binding.rcGallery.scrollToPosition(0)
                                binding.viewModel?.recyclerGalleryAdapter?.deselectAll()
                                binding.viewModel?.recyclerGalleryAdapter?.addItemToTop(galleryModel)
                                binding.viewModel?.showHideButtonDone(true)
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    R.string.error_while_capture_the_photo_or_video_empty_file,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@runOnUiThread
                        }
                    }
                } catch (e: Exception) {
                    Log.e("videoError", e.message!!)
                    e.printStackTrace()
                    Toast.makeText(
                        this@GalleryActivity,
                        R.string.error_while_capture_the_photo_or_video_empty_file,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


    override fun captureImage() {
        if (!checkPermissions())
            return
        val intent = Intent(this@GalleryActivity, CustomCameraActivity::class.java)
        imageResultLauncher.launch(intent)
    }

    var imageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK
            ) {
                // Tell the media scanner about the new file so that it is
                // immediately available to the user

                val fileImage = result.data?.extras?.get("ImageFile") as File
                if (fileImage != null) {
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(fileImage.toString()), null
                    ) { path, uri ->
                        runOnUiThread {
                            try {

                                // prepare model to send it
                                val customGalleryModel =
                                    GalleryModel()
                                customGalleryModel.index_when_selected = 1
                                val itemUrI = DataProvider().getImageURI(
                                    applicationContext, fileImage.path
                                )
                                val newUri = Uri.parse(itemUrI)
                                val galleryModel =
                                    binding.viewModel?.getLastCapturedGalleryImage(this, newUri)
                                if (galleryModel != null) {
                                    binding.viewModel?.selectedAlbumName?.value =
                                        getString(R.string.all)
                                    binding.viewModel?.recyclerGalleryAdapter?.filter("")
                                    binding.rcGallery.scrollToPosition(0)
                                    binding.viewModel?.recyclerGalleryAdapter?.deselectAll()
                                    binding.viewModel?.recyclerGalleryAdapter?.addItemToTop(
                                        galleryModel
                                    )
                                    binding.viewModel?.showHideButtonDone(true)
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        R.string.error_while_capture_the_photo_or_video_empty_file,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    applicationContext,
                                    R.string.error_while_capture_the_photo_or_video_empty_file,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }

    private fun getIntentForSelectedItems(
        selected: ArrayList<GalleryModel>?
    ) {
        // sort list as selected index
        if (selected != null) {
            Collections.sort(
                selected,
                ComparableGalleryModel.instance.compareIndex_when_selected
            )
        }
        val intent = Intent()
        intent.putExtra(GalleryConstants.selected, selected)
        setResult(RESULT_OK, intent)
    }

    // save instant to save file uri because the activity may be destroyed when
    // open camera
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        if (binding.viewModel?.mImageUri != null) {
//            outState.putString("cameraImageUri", binding.viewModel?.mImageUri.toString())
//        }
//    }

    // get the save instant
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        if (savedInstanceState.containsKey("cameraImageUri")) {
//            binding.viewModel?.mImageUri = Uri.parse(
//                savedInstanceState
//                    .getString("cameraImageUri")
//            )
//        }
//    }

    override fun onBackPressed() {
        finish_activity()
        super.onBackPressed()
    }
}