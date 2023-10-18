package hashim.gallerylib.imageviewer.viewer.rtlViewPager

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter


open class PagerAdapterWrapper(var adapter: PagerAdapter) : PagerAdapter() {
    fun getInnerAdapter(): PagerAdapter {
        return adapter
    }

    override fun getCount(): Int {
        return adapter.count
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return adapter.isViewFromObject(view, `object`)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return adapter.getPageTitle(position) ?: ""
    }

    override fun getPageWidth(position: Int): Float {
        return adapter.getPageWidth(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        return adapter.getItemPosition(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return adapter.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        adapter.destroyItem(container, position, `object`)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        adapter.setPrimaryItem(container, position, `object`)
    }

    override fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        adapter.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        adapter.unregisterDataSetObserver(observer)
    }

    override fun saveState(): Parcelable? {
        return adapter.saveState()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        adapter.restoreState(state, loader)
    }

    override fun startUpdate(container: ViewGroup) {
        adapter.startUpdate(container)
    }

    override fun finishUpdate(container: ViewGroup) {
        adapter.finishUpdate(container)
    }
}