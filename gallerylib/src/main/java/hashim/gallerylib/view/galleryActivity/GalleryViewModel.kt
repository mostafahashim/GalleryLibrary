package hashim.gallerylib.view.galleryActivity

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hashim.gallerylib.R
import hashim.gallerylib.adapter.RecyclerGalleryAdapter
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnItemSelectedListener
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ScreenSizeUtils
import java.io.File
import java.util.concurrent.TimeUnit

class GalleryViewModel : ViewModel() {
    lateinit var observer: Observer
    var selectedPhotos: ArrayList<GalleryModel> = ArrayList()
    var folderModels: ArrayList<String> = ArrayList()
    var showType = ""
    var maxSelectionCount = 10
    var selectedAlbumName = MutableLiveData("")
    var btnDoneVisible = MutableLiveData(false)
    var fromCameraContainer = MutableLiveData(false)
    var btnCameraPhotoVisible = MutableLiveData(false)
    var btnCameraVideoVisible = MutableLiveData(false)
    var isHasMedia = MutableLiveData(false)

    lateinit var tempCaptueredFile: File
    lateinit var mImageUri: Uri

    lateinit var recyclerGalleryAdapter: RecyclerGalleryAdapter


    var columnsNumber = MutableLiveData(0)
    fun initGalleryAdapter(screenWidth: Int) {
        val columnWidth = ((352.00 / columnsNumber.value!!) * screenWidth) / 360.00

        recyclerGalleryAdapter =
            RecyclerGalleryAdapter(columnWidth,
                maxSelectionCount,
                ArrayList(),
                object :
                    OnItemSelectedListener {
                    override fun onItemSelectedListener(position: Int) {
                        showHideButtonDone(getSelected().size > 0)
                    }

                    override fun onImageView(
                        position: Int,
                        imageView: ImageView,
                        galleryModels: ArrayList<GalleryModel>
                    ) {
                        observer.openImageViewer(position, imageView, galleryModels)
                    }
                }
            )
    }

    fun showHideButtonDone(isVisible: Boolean) {
        btnDoneVisible.value = isVisible
        fromCameraContainer.value = !isVisible
    }

    fun getSelected(): ArrayList<GalleryModel> {
        return recyclerGalleryAdapter.getSelected()
    }

    fun initializeCameraButtons() {
        if (showType.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
            btnCameraPhotoVisible.value = true
            btnCameraVideoVisible.value = false
        } else if (showType.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
            btnCameraPhotoVisible.value = false
            btnCameraVideoVisible.value = true
        } else if (showType.compareTo(GalleryConstants.GalleryTypeImagesAndVideos) == 0) {
            btnCameraPhotoVisible.value = true
            btnCameraVideoVisible.value = true
        }
    }

    @SuppressLint("Range")
    private fun getGalleryPhotos(context: Context): java.util.ArrayList<GalleryModel> {
        val galleryList = java.util.ArrayList<GalleryModel>()
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
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columnsImages, null, null, "$orderByimage DESC"
            )

            if (imagecursor != null && imagecursor.count > 0) {
                while (imagecursor.moveToNext()) {
                    val item = GalleryModel()
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
                    //get albumName
                    val albumNameColumnIndex = imagecursor
                        .getColumnIndex("bucket_display_name")
                    item.albumName = imagecursor.getString(albumNameColumnIndex)
                    if (!folderModels.contains(item.albumName))
                        folderModels.add(item.albumName)
                    galleryList.add(item)
                }
                imagecursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return galleryList
    }

    @SuppressLint("Range")
    private fun getGalleryVideos(context: Context): java.util.ArrayList<GalleryModel> {
        val galleryList = java.util.ArrayList<GalleryModel>()
        try {
            val columnsVideos =
                arrayOf(
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DATE_ADDED,
                    "bucket_id",
                    "bucket_display_name",
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                )
            val orderByVideo = MediaStore.Video.Media.DATE_MODIFIED
            val videoCursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                columnsVideos,
                null,
                null,
                "$orderByVideo DESC"
            )
            if (videoCursor != null && videoCursor.count > 0) {
                while (videoCursor.moveToNext()) {
                    val item = GalleryModel()
                    // get path
                    val dataColumnIndex = videoCursor
                        .getColumnIndex(MediaStore.Video.Media.DATA)
                    val videoPath = videoCursor
                        .getString(dataColumnIndex)
                    item.sdcardPath = videoPath
                    // get date modified
                    var dateColumnIndex = videoCursor
                        .getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                    var videoDate = videoCursor.getString(dateColumnIndex)
                    if (videoDate != null) {
                        item.item_date_modified = Integer.parseInt(videoDate)
                    } else {
                        dateColumnIndex = videoCursor
                            .getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                        videoDate = videoCursor.getString(dateColumnIndex)
                        if (videoDate != null) {
                            item.item_date_modified = Integer.parseInt(videoDate)
                        } else {
                            item.item_date_modified = SystemClock.elapsedRealtime().toInt()
                        }
                    }
                    //get video duration
                    val durationColumnIndex = videoCursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION)
                    val videoDuration = videoCursor.getString(durationColumnIndex)
                    if (videoDuration != null) {
                        item.videoDuration = videoDuration.toLong()
                        if (item.videoDuration > 0) {
                            item.videoDurationFormatted =
                                String.format(
                                    "%d:%d",
                                    TimeUnit.MILLISECONDS.toMinutes(item.videoDuration),
                                    TimeUnit.MILLISECONDS.toSeconds(item.videoDuration) -
                                            TimeUnit.MINUTES.toSeconds(
                                                TimeUnit.MILLISECONDS.toMinutes(
                                                    item.videoDuration
                                                )
                                            )
                                )
                        } else {
                            item.videoDurationFormatted = "00:00"
                        }
                    } else {
                        item.videoDuration = 0
                    }
                    //get Name
                    val nameColumnIndex = videoCursor
                        .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val name = videoCursor.getString(nameColumnIndex)
                    if (name != null) {
                        item.name = name
                    } else {
                        item.name = "unknown"
                    }
                    // get uri
                    val videoUri = ContentUris
                        .withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            videoCursor.getInt(
                                videoCursor
                                    .getColumnIndex(MediaStore.Video.Media._ID)
                            ).toLong()
                        )
                    item.itemUrI = videoUri.toString()
                    item.type = GalleryConstants.GalleryTypeVideos
                    //get albumName
                    val albumNameColumnIndex = videoCursor
                        .getColumnIndex("bucket_display_name")
                    item.albumName = videoCursor.getString(albumNameColumnIndex)
                    if (!folderModels.contains(item.albumName))
                        folderModels.add(item.albumName)
                    galleryList.add(item)
                }
                videoCursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return galleryList
    }

    fun getGalleryPhotosAndVideos(context: Context): ArrayList<GalleryModel> {
        var galleryList = ArrayList<GalleryModel>()
        try {
            folderModels = ArrayList()
            folderModels.add(context.getString(R.string.all))
            selectedAlbumName.value = folderModels[0]
            fromCameraContainer.value = true
            if (showType.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
                galleryList.addAll(getGalleryPhotos(context))
                btnCameraPhotoVisible.value = true
                btnCameraVideoVisible.value = false
            } else if (showType.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
                galleryList.addAll(getGalleryVideos(context))
                btnCameraPhotoVisible.value = false
                btnCameraVideoVisible.value = true
            } else if (showType.compareTo(GalleryConstants.GalleryTypeImagesAndVideos) == 0) {
                //get images
                galleryList.addAll(getGalleryPhotos(context))
                //get videos
                galleryList.addAll(getGalleryVideos(context))
                btnCameraPhotoVisible.value = true
                btnCameraVideoVisible.value = true
            }
            showHideButtonDone(selectedPhotos.size > 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // sort list by date modified
        galleryList =
            java.util.ArrayList<GalleryModel>(galleryList.sortedWith(compareBy { it.item_date_modified }))

//        Collections.sort(galleryList, ComparableGalleryModel.instance.compareDateModified)
        // showType newest photo at beginning of the list
        galleryList.reverse()
        // replace items that selected
        if (selectedPhotos.isNotEmpty()) {
            for (i in galleryList.indices) {
                for (j in selectedPhotos.indices) {
                    if (galleryList[i].sdcardPath
                            .compareTo(selectedPhotos[j].sdcardPath) == 0
                    ) {
                        galleryList[i] = selectedPhotos[j]
                    }
                }
            }
        }
        return galleryList
    }

    @SuppressLint("Range")
    fun getLastCapturedGalleryImage(context: Context, uri: Uri): GalleryModel? {
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
                //get albumName
                val albumNameColumnIndex = imagecursor
                    .getColumnIndex("bucket_display_name")
                item.albumName = imagecursor.getString(albumNameColumnIndex)
                if (!folderModels.contains(item.albumName))
                    folderModels.add(item.albumName)
                imagecursor.close()
                return item
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("Range")
    fun getLastCapturedGalleryVideo(context: Context, uri: Uri): GalleryModel? {
        try {
            val columnsVideos =
                arrayOf(
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DATE_ADDED,
                    "bucket_id",
                    "bucket_display_name",
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                )
            val orderByVideo = MediaStore.Video.Media.DATE_MODIFIED
            val videoCursor = context.contentResolver.query(
                uri/*MediaStore.Video.Media.EXTERNAL_CONTENT_URI*/,
                columnsVideos,
                null,
                null,
                "$orderByVideo DESC"
            )
            if (videoCursor != null && videoCursor.count > 0) {
                videoCursor.moveToFirst()
                val item = GalleryModel()
                item.index_when_selected = 1
                item.isSelected = true
                // get path
                val dataColumnIndex = videoCursor
                    .getColumnIndex(MediaStore.Video.Media.DATA)
                val videoPath = videoCursor
                    .getString(dataColumnIndex)
                item.sdcardPath = videoPath
                // get date modified
                var dateColumnIndex = videoCursor
                    .getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                var videoDate = videoCursor.getString(dateColumnIndex)
                if (videoDate != null) {
                    item.item_date_modified = Integer.parseInt(videoDate)
                } else {
                    dateColumnIndex = videoCursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    videoDate = videoCursor.getString(dateColumnIndex)
                    if (videoDate != null) {
                        item.item_date_modified = Integer.parseInt(videoDate)
                    } else {
                        item.item_date_modified = SystemClock.elapsedRealtime().toInt()
                    }
                }
                //get video duration
                val durationColumnIndex = videoCursor
                    .getColumnIndex(MediaStore.Video.Media.DURATION)
                val videoDuration = videoCursor.getString(durationColumnIndex)
                if (videoDuration != null) {
                    item.videoDuration = videoDuration.toLong()
                    if (item.videoDuration > 0) {
                        item.videoDurationFormatted =
                            String.format(
                                "%d:%d",
                                TimeUnit.MILLISECONDS.toMinutes(item.videoDuration),
                                TimeUnit.MILLISECONDS.toSeconds(item.videoDuration) -
                                        TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(
                                                item.videoDuration
                                            )
                                        )
                            )
                    } else {
                        item.videoDurationFormatted = "00:00"
                    }
                } else {
                    item.videoDuration = 0
                }
                //get Name
                val nameColumnIndex = videoCursor
                    .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val name = videoCursor.getString(nameColumnIndex)
                if (name != null) {
                    item.name = name
                } else {
                    item.name = "unknown"
                }
                // get uri
                val videoUri = ContentUris
                    .withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        videoCursor.getInt(
                            videoCursor
                                .getColumnIndex(MediaStore.Video.Media._ID)
                        ).toLong()
                    )
                item.itemUrI = videoUri.toString()
                item.type = GalleryConstants.GalleryTypeVideos
                //get albumName
                val albumNameColumnIndex = videoCursor
                    .getColumnIndex("bucket_display_name")
                item.albumName = videoCursor.getString(albumNameColumnIndex)
                if (!folderModels.contains(item.albumName))
                    folderModels.add(item.albumName)
                videoCursor.close()
                return item
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun checkImageStatus(context: Context) {
        if (recyclerGalleryAdapter.itemCount == 0) {
            //no media found
            isHasMedia.value = false
        } else {
            //media found
            isHasMedia.value = true
            //select album if all selected items in one album, else select all albums
            if (selectedPhotos.isEmpty()) {
                selectedAlbumName.value = context.getString(R.string.all)
                return
            }
            val albumName = selectedPhotos[0].albumName
            var sameAlbum = true
            for (i in selectedPhotos.indices) {
                if (selectedPhotos[i].albumName.compareTo(albumName) != 0) {
                    sameAlbum = false
                    break
                }
            }
            if (sameAlbum) {
                recyclerGalleryAdapter.filter(albumName)
                //change album name in dropdown
                selectedAlbumName.value = albumName
            } else {
                selectedAlbumName.value = context.getString(R.string.all)
            }
        }
    }

    interface Observer {
        fun openFinish()
        fun openAlbums()
        fun captureVideo()
        fun captureImage()
        fun onBackClicked()
        fun openImageViewer(
            position: Int,
            imageView: ImageView,
            galleryModels: java.util.ArrayList<GalleryModel>
        )
    }
}