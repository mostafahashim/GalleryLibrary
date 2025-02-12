package hashim.gallerylib.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.DisplayMetrics
import hashim.gallerylib.model.GalleryModel
import java.io.File
import java.io.Serializable
import java.util.Locale
import java.util.concurrent.TimeUnit

tailrec fun Context.activity(): Activity = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)!!.baseContext.activity()
}

fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            getSerializable(key, T::class.java)

        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }

inline fun <reified T : Serializable> Intent.serializable(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            getSerializableExtra(key, T::class.java)

        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
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
            val imageUri = ContentUris
                .withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imagecursor.getInt(
                        imagecursor
                            .getColumnIndex(MediaStore.Images.Media._ID)
                    ).toLong()
                )
            item.itemUrI = imageUri.toString()
            item.type = GalleryConstants.GalleryTypeImages
            item.url = "file://" + item.sdcardPath
            item.isVideo = false
            //get albumName
            val albumNameColumnIndex = imagecursor
                .getColumnIndex("bucket_display_name")
            item.albumName = imagecursor.getString(albumNameColumnIndex)
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
                            Locale.ENGLISH,
                            "%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(item.videoDuration),
                            TimeUnit.MILLISECONDS.toSeconds(item.videoDuration) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            item.videoDuration
                                        )
                                    ),
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
            item.url = Uri.fromFile(File(item.sdcardPath)).toString()
            item.isVideo = true
            //get albumName
            val albumNameColumnIndex = videoCursor
                .getColumnIndex("bucket_display_name")
            item.albumName = videoCursor.getString(albumNameColumnIndex)

            videoCursor.close()
            return item
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}
