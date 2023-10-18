package hashim.gallerylib.util

import android.app.Activity
import android.content.Context

tailrec fun Context.activity(): Activity = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)!!.baseContext.activity()
}