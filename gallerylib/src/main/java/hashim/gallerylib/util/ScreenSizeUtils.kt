package hashim.gallerylib.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.Point
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowInsets

import android.view.WindowMetrics

import android.os.Build
import androidx.core.view.WindowInsetsCompat


class ScreenSizeUtils {
    fun getItemWidth(activity: AppCompatActivity, numOfColumn: Double, isPadding: Boolean): Double {
        var columnWidth = 0.0
        val res = getScreenResolution(activity)
        val width = res[0]
        var padding = 0.0
        if (isPadding)
            padding = convertDpToPixel(25f).toDouble()
        columnWidth = (width - padding) / numOfColumn
        return columnWidth
    }

    fun getResolutionDensity(activity: AppCompatActivity): Float {
        return activity.resources.displayMetrics.density
    }

    fun getScreenResolution(activity: AppCompatActivity): IntArray {
        val sizeInpixels = intArrayOf(500, 960)
        return try {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            activity.windowManager.defaultDisplay
            display.getSize(size)
            sizeInpixels[0] = size.x
            sizeInpixels[1] = size.y
            sizeInpixels
        } catch (e: Exception) {
            sizeInpixels
        }
    }

    fun getScreenWidth(context: Context): Int {
        return try {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        } catch (e: Exception) {
            0
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        return try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        } catch (e: Exception) {
            0
        }
    }

    fun getScreenHeight(activity: Activity): Int {
        return try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.height() - insets.bottom - insets.top
            } else {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.heightPixels
            }
        } catch (e: Exception) {
            0
        }
    }

    fun convertPixelsToDp(px: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val dp = px / (metrics.densityDpi / 160f)
        return Math.round(dp).toFloat()
    }

    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }
}