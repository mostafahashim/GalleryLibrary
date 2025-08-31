package hashim.gallerylib.view.selected

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ActivitySelectedBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.ScreenSizeUtils
import hashim.gallerylib.util.dpToPx
import hashim.gallerylib.util.serializable
import hashim.gallerylib.view.GalleryBaseActivity
import hashim.gallerylib.view.crop.CropActivity

class SelectedActivity :
    GalleryBaseActivity(
        R.string.gallery, false, true, true, false,
        false, true, true
    ), SelectedViewModel.Observer {
    lateinit var binding: ActivitySelectedBinding
    override fun doOnCreate(arg0: Bundle?) {
        binding = putContentView(R.layout.activity_selected) as ActivitySelectedBinding
        binding.viewModel = ViewModelProvider(this)[SelectedViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
        initializeViews()
        setListener()
    }

    override fun initializeViews() {
        binding.viewModel?.selectedPhotos =
            intent.extras?.serializable<ArrayList<GalleryModel>>(GalleryConstants.selected)
                ?: ArrayList()
        initViewPager()
        binding.viewModel?.selectedPhotos?.forEach { item ->
            item.isSelected = false
        }
        binding.viewModel?.isItemsMoreThanOne?.value = binding.viewModel?.selectedPhotos?.size!! > 1
        if (binding.viewModel?.isItemsMoreThanOne?.value == true) {
            binding.viewModel?.initThumbnailsAdapter(ScreenSizeUtils().getScreenWidth(this))
            binding.viewModel?.selectOnlyItem(0)
        }
    }

    val pagerAutoScrollHandler = Handler(Looper.getMainLooper())
    private fun initViewPager() {
        binding.viewPagerActivityLanguage.visibility = View.VISIBLE
        binding.viewPagerActivityLanguage.apply {
            clipToPadding = false   // allow full width shown with padding
            clipChildren = false    // allow left/right item is not clipped
            offscreenPageLimit = 1  // make sure left/right item is rendered
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                //set items padding, when more padding, item close to other
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                clipToPadding = false
                //adapter
                binding.viewModel?.selectedPagerAdapter =
                    RecyclerSelectedPagerAdapter(
                        binding.viewModel?.selectedPhotos!!
                    )
                adapter = binding.viewModel?.selectedPagerAdapter
            }
            val offsetPx =
                resources.getDimension(R.dimen.padding_0).toInt()
                    .dpToPx(resources.displayMetrics)
            setPadding(0, 0, offsetPx, 0)
            val pageMarginPx =
                resources.getDimension(R.dimen.padding_1).toInt()
                    .dpToPx(resources.displayMetrics)
            val marginTransformer = MarginPageTransformer(pageMarginPx)
            setPageTransformer(marginTransformer)
        }

        binding.viewPagerActivityLanguage.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            var previousPosition = -1
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) pagerAutoScrollHandler.removeMessages(
                    0
                )
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val photos = binding.viewModel?.selectedPhotos ?: return
                if (photos.isEmpty()) return

                // check if prev position inside list
                if (previousPosition in photos.indices &&
                    photos[previousPosition].type == GalleryConstants.GalleryTypeVideos
                ) {
                    photos[previousPosition].isReleasePlayer = true
                    binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(previousPosition)
                }

                // check if position inside list
                if (position in photos.indices) {
                    if (photos[position].type == GalleryConstants.GalleryTypeVideos) {
                        photos[position].isReleasePlayer = false
                        binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(position)
                    }

                    // hide/show crop button
                    binding.imgviewCrop.visibility =
                        if (photos[position].type == GalleryConstants.GalleryTypeImages) {
                            View.VISIBLE
                        } else View.GONE

                    // select item
                    binding.viewModel?.selectOnlyItem(position,true)
                    previousPosition = position
                } else {
                    previousPosition = -1
                }
            }

        })
    }

    override fun animateToPagerPosition(position: Int) {
        binding.viewPagerActivityLanguage.setCurrentItem(position, true)
    }

    override fun animateRecyclerViewToPosition(position: Int) {
        binding.recyclerSelectedItemsThumbnails.scrollToPosition(position)
    }

    override fun getCurrentPagerPosition(): Int {
        return binding.viewPagerActivityLanguage.currentItem
    }

    override fun setListener() {
    }

    override fun cropImage() {
        if (!binding.viewModel?.selectedPhotos.isNullOrEmpty()
            && binding.viewModel?.selectedPhotos!![binding.viewPagerActivityLanguage.currentItem].type.compareTo(
                GalleryConstants.GalleryTypeImages
            ) == 0
        ) {
            Intent(this, CropActivity::class.java).also {
                val galleryModels = ArrayList<GalleryModel>()
                galleryModels.add(binding.viewModel?.selectedPhotos!![binding.viewPagerActivityLanguage.currentItem])
                it.putExtra("GalleryModels", galleryModels)
                it.putExtra("position", binding.viewPagerActivityLanguage.currentItem)
                var locale = "en"
                if (intent.hasExtra(GalleryConstants.Language))
                    locale =
                        intent.getStringExtra(GalleryConstants.Language) ?: GalleryConstants.ENGLISH
                it.putExtra(GalleryConstants.Language, locale)
                cropResultLauncher.launch(it)
            }
        }
    }

    private var cropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val position = result.data?.extras?.getInt("position", -1) ?: -1
                if (position != -1) {
                    val galleryModels =
                        result.data?.extras?.serializable<ArrayList<GalleryModel>>("GalleryModels")
                            ?: ArrayList()
                    if (galleryModels.isNotEmpty()) {
                        binding.viewModel?.selectedPhotos!![position] = galleryModels[0]
                        binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(position)
                    }
                }
            }

        }

    override fun onBackClicked() {
        finish_activity()
    }

    override fun finishWithSuccess() {
        for (i in binding.viewModel?.selectedPhotos!!.indices) {
            binding.viewModel?.selectedPhotos!![i].player = null
        }
        val intent = Intent()
        intent.putExtra(GalleryConstants.selected, binding.viewModel?.selectedPhotos)
        setResult(RESULT_OK, intent)
        finish_activity()
    }

}