package hashim.gallerylib.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import hashim.gallerylib.R
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class DataProvider {
    var AUDIO_FOLDER = "Audios"
    var IMAGE_FOLDER = "Photos"
    var VIDEO_FOLDER = "Videos"
    var TEMP_FOLDER = "Temp"
    var GalleryTypeImages = "Images"
    var GalleryTypeVideos = "Videos"

    companion object {
        val TYPE_PHOTO_INTERNAL = "2"
        val TYPE_VIDEO_INTERNAL = "4"
        val TYPE_TEMP = "12"
    }

    @SuppressLint("Range")
    fun getThumbnail(activity: Activity, path: String): Bitmap {
        val cr = activity.contentResolver
        val ca = cr.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DATA + "=?",
            arrayOf(path),
            null
        )
        if (ca != null && ca.moveToFirst()) {
            val id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID))
            ca.close()
            return MediaStore.Images.Thumbnails.getThumbnail(
                cr,
                id.toLong(),
                MediaStore.Images.Thumbnails.MINI_KIND,
                null
            )
        }

        ca!!.close()
        // return BitmapFactory.decodeFile(path);
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 600, 400)
    }

    fun deleteTempDir(context: Context) {

        val _filepath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)?.path
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + context.getString(
                R.string.app_name
            )
        }
        val dir =
            File(
                "$_filepath/" + context.resources.getString(
                    R.string.app_name
                ) + "/" + context.resources.getString(
                    R.string.app_name
                ) + " " + TEMP_FOLDER
            )
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children!!.indices) {
                File(dir, children[i]).delete()
            }
        }
        dir.delete()
    }

    fun getFilePath(type: String, context: Context): String {
        val _filepath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)?.path
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + context.getString(
                R.string.app_name
            )
        }
        var file: File? = null
        var extention: String? = null
        var Type = ""
        when (type) {
            TYPE_VIDEO_INTERNAL -> {
                file = File(
                    _filepath,
                    context.resources.getString(R.string.app_name) + "/" + context.resources.getString(
                        R.string.app_name
                    ) + " " + VIDEO_FOLDER
                )
                extention = ".mp4"
                Type = "VID_"
            }

            TYPE_PHOTO_INTERNAL -> {
                file = File(
                    _filepath,
                    context.resources.getString(R.string.app_name) + "/" + context.resources.getString(
                        R.string.app_name
                    ) + " " + IMAGE_FOLDER
                )
                extention = ".jpg"
                Type = "IMG_"
            }

            else -> {
            }
        }

        if (!file!!.exists()) {
            file.mkdirs()
        }
        val r = Random()
        val Low = 10000
        val High = 1000000000
        val R = r.nextInt(High - Low) + Low
        return (file.absolutePath + "/" + Type + System.currentTimeMillis() + "_" + R + extention)
    }

    @SuppressLint("SimpleDateFormat")
    fun getFile(type: String, context: Context): File {
        val _filepath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)?.path
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + context.getString(
                R.string.app_name
            )
        }
        var file: File? = null
        var extention: String? = null
        var Type = ""
        when (type) {
            TYPE_VIDEO_INTERNAL -> {
                file = File(
                    _filepath,
                    context.resources.getString(R.string.app_name) + "/" + context.resources.getString(
                        R.string.app_name
                    ) + " " + VIDEO_FOLDER
                )
                extention = ".mp4"
                Type = "VID_"
            }

            TYPE_PHOTO_INTERNAL -> {
                file = File(
                    _filepath,
                    context.resources.getString(R.string.app_name) + "/" + context.resources.getString(
                        R.string.app_name
                    ) + " " + IMAGE_FOLDER
                )
                extention = ".jpg"
                Type = "IMG_"
            }

            TYPE_TEMP -> {
                file = File(
                    _filepath,
                    context.resources.getString(R.string.app_name) + "/" + context.resources.getString(
                        R.string.app_name
                    ) + " " + TEMP_FOLDER
                )
                extention = ".jpg"
                Type = "IMG_"
            }

            else -> {
            }
        }

        if (!file!!.exists()) {
            file.mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        file = File(file.toString() + File.separator + Type + timeStamp + extention)
        return file
    }


    fun getRandomName(type: String, path: String): String {
        val r = Random()
        val Low = 10000
        val High = 1000000000
        val R = r.nextInt(High - Low) + Low
        var extention: String? = null
        var Type = ""
        when (type) {
            TYPE_VIDEO_INTERNAL -> {
                extention = ".mp4"
                Type = "VID_"
            }

            TYPE_PHOTO_INTERNAL -> {
                extention = ".jpg"
                Type = "IMG_"
            }

            else -> {
                val arr = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (arr.size > 1) extention = "." + arr[arr.size - 1]
                else {
                    extention = ""
                }
            }
        }
        return Type + System.currentTimeMillis() + "_" + R + extention
    }

    fun saveImage(finalBitmap: Bitmap, context: Context): String {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var imageUri = addImageToGallery("", context)
                if (imageUri != null) {
                    var fos = context.contentResolver.openOutputStream(imageUri) as FileOutputStream
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                    fos.close()
//                    val file = File(FileUtils(context).getPath(imageUri))
                    var filePath = getImagePath(context, imageUri)
                    if (filePath != null) {
                        val file = File(filePath)
                        return file.path
                    }

                }
            } else {
                val path = getFilePath(TYPE_PHOTO_INTERNAL, context)
                var imageUri = addImageToGallery(path, context)
                if (imageUri != null) {
                    var filePath = getImagePath(context, imageUri)
                    if (filePath != null) {
                        val file = File(filePath)
                        var fos = FileOutputStream(file)
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.flush()
                        fos.close()
                        return path
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun addImageToGallery(filePath: String, context: Context): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis())
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + context.resources.getString(
                    R.string.app_name
                )
                //values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/appFolder");
                //values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/appFolder");
            )
        } else
            values.put(MediaStore.MediaColumns.DATA, filePath)
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }


    private fun getImagePath(context: Context, uri: Uri): String? {
        try {
            val columnsImages = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_ADDED,
                "bucket_id",
                "bucket_display_name",
                MediaStore.Images.Media._ID
            )
            val orderByimage = MediaStore.Images.Media.DATE_MODIFIED
            val imagecursor = context.contentResolver.query(
                uri,
                columnsImages, null, null, "$orderByimage"
            )
            if (imagecursor != null && imagecursor.count > 0) {
                imagecursor.moveToFirst()
                // get path
                val dataColumnIndex = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.DATA)
                var sdcardPath = imagecursor.getString(dataColumnIndex)
                return sdcardPath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @SuppressLint("Range")
    fun getImageURI(context: Context, filePath: String): String {
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )

        var imageuri = ""
        if (cursor != null && cursor.moveToFirst()) {
            imageuri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)).toLong()
            ).toString()
        }
        return imageuri
    }

    fun deleteFileFromMediaStore(contentResolver: ContentResolver, file: File) {
        val canonicalPath: String
        canonicalPath = try {
            file.canonicalPath
        } catch (e: IOException) {
            file.absolutePath
        }
        val uri = MediaStore.Files.getContentUri("external")
        val result = contentResolver.delete(
            uri,
            MediaStore.Files.FileColumns.DATA + "=?", arrayOf(canonicalPath)
        )
        if (result == 0) {
            val absolutePath = file.absolutePath
            if (absolutePath != canonicalPath) {
                contentResolver.delete(
                    uri,
                    MediaStore.Files.FileColumns.DATA + "=?", arrayOf(absolutePath)
                )
            }
        }
    }

    fun getImagePathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun getVideoPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun getOriginalRotationForBitmap(filePath: String, bitmap: Bitmap?): Bitmap? {
        var bitmap = bitmap
        val ei: ExifInterface
        try {
            ei = ExifInterface(filePath)
            val orientation =
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_UNDEFINED -> bitmap = rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    bitmap = rotateBitmap(bitmap, 180)
                    bitmap = rotateBitmap(bitmap, -90)
                }

                ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = rotateBitmap(bitmap, -90)
                ExifInterface.ORIENTATION_NORMAL -> bitmap = rotateBitmap(bitmap, 0)
                ExifInterface.ORIENTATION_TRANSVERSE -> bitmap = rotateBitmap(bitmap, -90)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun rotateBitmap(source: Bitmap?, mRotation: Int): Bitmap {
        val targetWidth = source!!.width
        val targetHeight = source.height
        /*Bitmap targetBitmap = Bitmap.createBitmap(targetHeight, targetWidth,
                Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(source, TileMode.CLAMP,
                TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        matrix.setRotate(mRotation, source.getHeight(),
                source.getWidth());
        canvas.drawBitmap(source, matrix, paint);*/
        //return targetBitmap;

        val matrix = Matrix()
        matrix.postRotate(mRotation.toFloat())
        return Bitmap.createBitmap(source, 0, 0, targetWidth, targetHeight, matrix, true)

    }

    fun decodeImage(file: File, context: Context): File? {
        try {
            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image

            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 75

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)

            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            // Store to tmp file
            val tempFile = DataProvider()
                .getFile(TYPE_TEMP, context)
            //            tempFile.createNewFile();
            val outputStream = FileOutputStream(tempFile)

            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            return tempFile
        } catch (e: Exception) {
            return null
        }

    }

    fun getBitmapFromURL(url: String): Bitmap? {
        var bm: Bitmap? = null
        val options = BitmapFactory.Options()
        try {
            val urlLink = java.net.URL(url)
            bm = BitmapFactory.decodeStream(urlLink.openConnection().getInputStream())
            //            if (pfd != null) {
            //                FileDescriptor fd = pfd.getFileDescriptor();
            //                bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
            //            }
        } catch (ee: IOException) {
            ee.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bm
    }

    // resize the bitmap
    fun decodeImage(
        path: String,
        DESIREDWIDTH: Int,
        DESIREDHEIGHT: Int,
        temp: Boolean,
        context: Context
    ): String? {
        var strMyImagePath: String? = null
        var scaledBitmap: Bitmap? = null
        try {
            // Part 1: Decode image
            val unscaledBitmap =
                ScalingUtilities()
                    .decodeFile(
                        path, DESIREDWIDTH, DESIREDHEIGHT,
                        ScalingUtilities.ScalingLogic.FIT
                    )

            if (unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities()
                    .createScaledBitmap(
                        unscaledBitmap,
                        DESIREDWIDTH,
                        DESIREDHEIGHT,
                        ScalingUtilities.ScalingLogic.FIT
                    )
            } else {
                unscaledBitmap.recycle()
                return path
            }

            // Store to tmp file
            val tempFile: File
            if (temp) tempFile = DataProvider()
                .getFile(TYPE_TEMP, context)
            else tempFile = DataProvider()
                .getFile(TYPE_PHOTO_INTERNAL, context)
            strMyImagePath = tempFile.absolutePath
            scaledBitmap = getOriginalRotationForBitmap(path, scaledBitmap)
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tempFile)
                scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {

                e.printStackTrace()
            } catch (e: Exception) {

                e.printStackTrace()
            }

            scaledBitmap!!.recycle()
        } catch (e: Throwable) {
        }

        return strMyImagePath ?: path
    }
}