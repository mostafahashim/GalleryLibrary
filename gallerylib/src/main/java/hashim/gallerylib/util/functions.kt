package hashim.gallerylib.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import java.io.Serializable

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