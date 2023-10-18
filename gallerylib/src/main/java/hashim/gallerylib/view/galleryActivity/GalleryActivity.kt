package hashim.gallerylib.view.galleryActivity


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hashim.gallerylib.R
import hashim.gallerylib.adapter.RecyclerGalleryAdapter
import hashim.gallerylib.databinding.ActivityGalleryBinding
import hashim.gallerylib.imageviewer.ImageViewer
import hashim.gallerylib.imageviewer.listeners.OnDismissListener
import hashim.gallerylib.imageviewer.listeners.OnImageChangeListener
import hashim.gallerylib.imageviewer.loader.ImageLoader
import hashim.gallerylib.imageviewer.viewer.viewholder.ViewHolderLoader
import hashim.gallerylib.model.ComparableGalleryModel
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnBottomSheetItemClickListener
import hashim.gallerylib.util.DataProvider
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ScreenSizeUtils
import hashim.gallerylib.view.GalleryBaseActivity
import hashim.gallerylib.view.sub.BottomSheetAlbumsFragment
import java.util.*


class GalleryActivity : GalleryBaseActivity(
    R.string.gallery, false, true, true, false,
    false, true, true
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

        binding.viewModel?.initGalleryAdapter(ScreenSizeUtils().getScreenWidth(this))
//        binding.viewModel?.updateBooksAdapterColumnWidth(ScreenSizeUtils().getScreenWidth(this))
        binding.viewModel?.initializeCameraButtons()
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
                    binding.viewModel?.getGalleryPhotosAndVideos(this as Context) ?: ArrayList()
                )
                binding.viewModel?.checkImageStatus(this)
            }
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

    private var videoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && binding.viewModel?.tempCaptueredFile != null) {
                try {
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(binding.viewModel?.tempCaptueredFile.toString()), null
                    ) { _, uri ->
                        runOnUiThread {
                            val newUri = uri
                                ?: if (result.data != null && result.data?.data != null) {
                                    result.data?.data!!
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        binding.viewModel?.tempCaptueredFile = DataProvider()
            .getFile(DataProvider.TYPE_PHOTO_INTERNAL, applicationContext)

        if (binding.viewModel?.tempCaptueredFile != null) {
            val photoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    binding.viewModel?.tempCaptueredFile!!
                )
            } else {
                Uri.fromFile(binding.viewModel?.tempCaptueredFile)
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            imageResultLauncher.launch(takePictureIntent)
        }
//        if (!checkPermissions())
//            return
//        val intent = Intent(this@GalleryActivity, CameraActivity::class.java)
//        imageResultLauncher.launch(intent)
    }

    private var imageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && binding.viewModel?.tempCaptueredFile != null) {
                try {
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(binding.viewModel?.tempCaptueredFile.toString()), null
                    ) { _, uri ->
                        runOnUiThread {
                            val newUri = uri
                                ?: if (result.data != null && result.data?.data != null) {
                                    result.data?.data!!
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                                binding.viewModel?.getLastCapturedGalleryImage(this, newUri)
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

    /*  private var imageResultLauncher =
          registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
              if (result.resultCode == Activity.RESULT_OK
              ) {
                  // Tell the media scanner about the new file so that it is
                  // immediately available to the user

                  val fileImage = result.data?.extras?.get("ImageFile") as File
                  MediaScannerConnection.scanFile(
                      this,
                      arrayOf(fileImage.toString()), null
                  ) { _, _ ->
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
          }*/

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

    override fun onBackPressed() {
        finish_activity()
        super.onBackPressed()
    }

    private var viewer: ImageViewer<GalleryModel>? = null
    override fun openImageViewer(
        position: Int,
        imageView: ImageView,
        galleryModels: ArrayList<GalleryModel>
    ) {
        //new list without first item because it add button
        if (galleryModels.isEmpty())
            return

        viewer = ImageViewer.Companion.Builder(
            this,
            galleryModels,
            object : ImageLoader<GalleryModel> {
                override fun loadImage(imageView: ImageView?, model: GalleryModel?) {
                        Glide.with(this@GalleryActivity).load(model?.url)
                            .into(imageView!!)
                }
            },
//            CustomViewHolder.buildViewHolder(imageView)
        ).withStartPosition(position)
            .withTransitionFrom(imageView)
            .withImageChangeListener(object : OnImageChangeListener {
                override fun onImageChange(position: Int) {
                    binding.rcGallery.scrollToPosition(position)
                    val holder: RecyclerView.ViewHolder? =
                        binding.rcGallery.findViewHolderForAdapterPosition(position)
                    if (holder != null && holder is RecyclerGalleryAdapter.ViewHolder) {
                        viewer?.updateTransitionImage((holder.binding.ivImage))
                    }

//                    viewer?.updateTransitionImage(imageView)
                }
            }).withDismissListener(object : OnDismissListener {
                override fun onDismiss() {
                    //do any thing when dismiss
                }
            }).withHiddenStatusBar(false)
            .withImagesMargin(R.dimen.padding_10)
            .withContainerPadding(R.dimen.padding_0)
            .allowZooming(true)
            .allowSwipeToDismiss(true)
            .show()

    }
}