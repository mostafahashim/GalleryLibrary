package hashim.gallerylib.imageviewer.common.extensions

import android.util.SparseArray
import java.util.ConcurrentModificationException

internal inline fun <T> SparseArray<T>.forEach(block: (Int, T) -> Unit) {
    val size = this.size()
    for (index in 0 until size) {
        if (size != this.size()) throw ConcurrentModificationException()
        block(this.keyAt(index), this.valueAt(index))
    }
}