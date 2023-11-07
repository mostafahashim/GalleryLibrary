package hashim.gallerylib.util

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics

tailrec fun Context.activity(): Activity = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)!!.baseContext.activity()
}

fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()