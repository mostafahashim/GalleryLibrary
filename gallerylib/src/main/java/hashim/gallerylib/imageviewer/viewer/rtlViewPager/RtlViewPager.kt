package hashim.gallerylib.imageviewer.viewer.rtlViewPager

import android.content.Context
import android.database.DataSetObserver
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


open class RtlViewPager : ViewPager {
    private lateinit var reverseOnPageChangeListeners: MutableMap<OnPageChangeListener, ReverseOnPageChangeListener>

    private var dataSetObserver: DataSetObserver? = null

    private var suppressOnPageChangeListeners = false

    constructor(context: Context) : super(context) {
        reverseOnPageChangeListeners = ArrayMap(1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        reverseOnPageChangeListeners = ArrayMap(1)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerRtlDataSetObserver(super.getAdapter()!!)
    }

    override fun onDetachedFromWindow() {
        unregisterRtlDataSetObserver()
        super.onDetachedFromWindow()
    }

    private fun registerRtlDataSetObserver(adapter: PagerAdapter) {
        if (adapter is ReverseAdapter && dataSetObserver == null) {
            dataSetObserver = RevalidateIndicesOnContentChange(adapter)
            adapter.registerDataSetObserver(dataSetObserver!!)
            adapter.revalidateIndices()
        }
    }

    private fun unregisterRtlDataSetObserver() {
        val adapter = super.getAdapter()
        if (adapter is ReverseAdapter && dataSetObserver != null) {
            adapter.unregisterDataSetObserver(dataSetObserver!!)
            dataSetObserver = null
        }
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(convert(item), smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(convert(item))
    }

    override fun getCurrentItem(): Int {
        return convert(super.getCurrentItem())
    }

    private fun convert(position: Int): Int {
        return if (position >= 0 && isRtl()) {
            adapter.count - position - 1
        } else {
            position
        }
    }

    override fun getAdapter(): PagerAdapter {
        val adapter = super.getAdapter()
        return if (adapter is ReverseAdapter) adapter.getInnerAdapter() else adapter!!
    }

    override fun fakeDragBy(xOffset: Float) {
        super.fakeDragBy(if (isRtl()) xOffset else -xOffset)
    }

    override fun setAdapter(adapter1: PagerAdapter?) {
        var adapter = adapter1
        unregisterRtlDataSetObserver()
        val rtlReady = isRtl()
        if (rtlReady) {
            adapter = ReverseAdapter(adapter!!)
            registerRtlDataSetObserver(adapter)
        }
        super.setAdapter(adapter)
        if (rtlReady) {
            setCurrentItemWithoutNotification(0)
        }
    }

    private fun setCurrentItemWithoutNotification(index: Int) {
        suppressOnPageChangeListeners = true
        setCurrentItem(index, false)
        suppressOnPageChangeListeners = false
    }

    protected fun isRtl(): Boolean {
        return TextUtilsCompat.getLayoutDirectionFromLocale(
            context.resources.configuration.locale
        ) == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    override fun addOnPageChangeListener(listener1: OnPageChangeListener) {
        var listener = listener1
        if (isRtl()) {
            val reverseListener = ReverseOnPageChangeListener(listener)
            reverseOnPageChangeListeners[listener] = reverseListener
            listener = reverseListener
        }
        super.addOnPageChangeListener(listener)
    }

    override fun removeOnPageChangeListener(listener1: OnPageChangeListener) {
        var listener = listener1
        if (isRtl()) {
            listener = reverseOnPageChangeListeners.remove(listener)!!
        }
        super.removeOnPageChangeListener(listener)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState(), currentItem, isRtl())
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        if (ss.isRTL != isRtl()) setCurrentItem(ss.position, false)
    }

    private inner class ReverseAdapter(adapter: PagerAdapter) : PagerAdapterWrapper(adapter) {
        private var lastCount: Int

        init {
            lastCount = adapter.count
        }

        override fun getPageTitle(position: Int): CharSequence {
            return super.getPageTitle(reverse(position))
        }

        override fun getPageWidth(position: Int): Float {
            return super.getPageWidth(reverse(position))
        }

        override fun getItemPosition(`object`: Any): Int {
            val itemPosition: Int = super.getItemPosition(`object`)
            return if (itemPosition < 0) itemPosition else reverse(itemPosition)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return super.instantiateItem(container, reverse(position))
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, reverse(position), `object`)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, lastCount - position - 1, `object`)
        }

        private fun reverse(position: Int): Int {
            return count - position - 1
        }

        fun revalidateIndices() {
            val newCount: Int = count
            if (newCount != lastCount) {
                setCurrentItemWithoutNotification(Math.max(0, lastCount - 1))
                lastCount = newCount
            }
        }
    }

    private class RevalidateIndicesOnContentChange(private val adapter: ReverseAdapter) :
        DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            adapter.revalidateIndices()
        }
    }

    private inner class ReverseOnPageChangeListener(private val original: OnPageChangeListener) :
        OnPageChangeListener {
        private var pagerPosition: Int

        init {
            pagerPosition = -1
        }

        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) {
            if (!suppressOnPageChangeListeners) {
                pagerPosition = if (positionOffset == 0f && positionOffsetPixels == 0) {
                    reverse(position)
                } else {
                    reverse(position + 1)
                }
                original.onPageScrolled(
                    pagerPosition,
                    if (positionOffset > 0) 1f - positionOffset else positionOffset,
                    positionOffsetPixels
                )
            }
        }

        override fun onPageSelected(position: Int) {
            if (!suppressOnPageChangeListeners) {
                original.onPageSelected(reverse(position))
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (!suppressOnPageChangeListeners) {
                original.onPageScrollStateChanged(state)
            }
        }

        private fun reverse(position: Int): Int {
            val adapter: PagerAdapter = getAdapter()
            return if (adapter == null) position else adapter.count - position - 1
        }
    }

    class SavedState : Parcelable {
        var superState: Parcelable?
        var position: Int
        var isRTL: Boolean

        constructor(superState: Parcelable?, position: Int, isRTL: Boolean) : super() {
            this.superState = superState
            this.position = position
            this.isRTL = isRTL
        }

        internal constructor(`in`: Parcel, loader: ClassLoader?) {
            var loader = loader
            if (loader == null) {
                loader = javaClass.classLoader
            }
            superState = `in`.readParcelable(loader)
            position = `in`.readInt()
            isRTL = `in`.readByte().toInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeParcelable(superState, flags)
            out.writeInt(position)
            out.writeByte(if (isRTL) 1.toByte() else 0.toByte())
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            @JvmField
            val CREATOR: ClassLoaderCreator<SavedState> = object : ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(source, loader)
                }

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source, null)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}